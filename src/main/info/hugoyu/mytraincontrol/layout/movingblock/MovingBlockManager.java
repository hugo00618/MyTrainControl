package info.hugoyu.mytraincontrol.layout.movingblock;

import info.hugoyu.mytraincontrol.layout.Position;
import info.hugoyu.mytraincontrol.layout.Route;
import info.hugoyu.mytraincontrol.layout.Vector;
import info.hugoyu.mytraincontrol.layout.node.impl.StationTrackNode;
import info.hugoyu.mytraincontrol.sensor.SensorState;
import info.hugoyu.mytraincontrol.trainset.Trainset;
import info.hugoyu.mytraincontrol.util.LayoutUtil;
import info.hugoyu.mytraincontrol.util.RouteUtil;
import lombok.Getter;

import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static info.hugoyu.mytraincontrol.sensor.SensorState.EXIT;

@Getter
public class MovingBlockManager {

    private Trainset trainset;

    private List<Long> nodesToAllocate;

    private long destinationId;
    private final Object destinationLock = new Object();

    private double allocatedMoveDist;
    private double distToMove; // total distance to move
    private double calibrationOffset;
    private final Lock distToMoveLock = new ReentrantLock(true);

    private int distToAlloc; // total distance to alloc

    private int distToFree; // total distance to free
    private final Object distToFreeLock = new Object();

    private double movedDistToFree;

    private boolean isStopRoutineInitiated;
    private final Object isStopRoutineInitiatedLock = new Object();

    private boolean isUplink;

    public MovingBlockManager(Trainset trainset) {
        this.trainset = trainset;
    }

    public void prepareToMove(Route route) {
        this.nodesToAllocate = route.getNodes();
        this.isUplink = route.isUplink();
        setDestinationId(nodesToAllocate.get(nodesToAllocate.size() - 1));

        int distToMove = calcDistToMove(trainset, route);
        this.distToMove += distToMove;
        this.distToAlloc += distToMove;
        this.distToFree += distToMove;
    }

    public Runnable getNewMovingBlockRunnable() {
        return new MovingBlockRunnable(this);
    }

    public void calibrate(Position sensorPosition, SensorState sensorState) {
        final Vector referenceNodeVector = sensorPosition.getReferenceNodeVector();
        Route remainingRoute = RouteUtil.findRoute(referenceNodeVector.getId0(), getDestinationId(), isUplink);
        int calibratedDistToMove = calcCalibratedDistToMove(trainset, remainingRoute, sensorPosition, sensorState);

        double offset;
        distToMoveLock.lock();
        try {
            offset = calibratedDistToMove - distToMove;

            calibrationOffset += offset;
            distToMove = calibratedDistToMove;
        } finally {
            distToMoveLock.unlock();
        }
        trainset.addDistToMove(offset);
    }

    private int calcCalibratedDistToMove(Trainset trainset, Route remainingRoute, Position sensorPosition, SensorState sensorState) {
        // remainingRoute.getCost() is the length between sensor's reference node to our destination node
        int calibratedDistToMove = remainingRoute.getCost();
        // if our travelling direction is the same with sensor's definition,
        // then we need to subtract offset from the total cost, otherwise we add
        if (isUplink == sensorPosition.isUplink()) {
            calibratedDistToMove -= sensorPosition.getOffset();
        } else {
            calibratedDistToMove += sensorPosition.getOffset();
        }

        if (isStopRoutineInitiated) {
            StationTrackNode stationTrackNode = LayoutUtil.getStationTrackNode(remainingRoute.getDestinationVector());
            calibratedDistToMove += stationTrackNode.getInboundMoveDist(trainset);
        }
        if (sensorState == EXIT) {
            calibratedDistToMove -= trainset.getTotalLength();
        }

        return calibratedDistToMove;
    }

    private int calcDistToMove(Trainset trainset, Route route) {
        StationTrackNode stationTrackNode = LayoutUtil.getStationTrackNode(
                new Vector(nodesToAllocate.get(0), nodesToAllocate.get(1)));
        int stationOutBoundMoveDist = stationTrackNode.getInboundMoveDist(trainset);
        if (isUplink == stationTrackNode.isUplink()) {
            return route.getCost() - stationOutBoundMoveDist;
        } else {
            return route.getCost() + stationOutBoundMoveDist;
        }
    }

    public void addDistToMove(double dist) {
        distToMoveLock.lock();
        try {
            distToMove += dist;
        } finally {
            distToMoveLock.unlock();
        }
    }

    public double getDistToMove() {
        distToMoveLock.lock();
        try {
            return distToMove;
        } finally {
            distToMoveLock.unlock();
        }
    }

    public double getAllocatedMoveDist() {
        distToMoveLock.lock();
        try {
            return allocatedMoveDist;
        } finally {
            distToMoveLock.unlock();
        }
    }

    public void addDistToFree(int dist) {
        synchronized (distToFreeLock) {
            distToFree += dist;
        }
    }

    public int getDistToFree() {
        synchronized (distToFreeLock) {
            return distToFree;
        }
    }

    public void addAllocatedMoveDist(double dist) {
        distToMoveLock.lock();
        try {
            allocatedMoveDist += dist;
        } finally {
            distToMoveLock.unlock();
        }
    }

    /**
     * called from MovingBlockRunnable to log the distance that the train has moved, which does
     * 1. subtract dist from distToMove
     * 2. subtract dist from calibrationOffset, if it's positive
     *
     * @param movedDist
     * @return movedDist that should expose to the block section system
     */
    public double logMovedDist(double movedDist) {
        distToMoveLock.lock();
        try {
            distToMove -= movedDist;

            if (calibrationOffset >= movedDist) {
                calibrationOffset -= movedDist;
                return 0;
            } else {
                double res = movedDist - calibrationOffset;
                calibrationOffset = 0;
                return res;
            }
        } finally {
            distToMoveLock.unlock();
        }
    }

    public void setIsStopRoutineInitiated(boolean isStopRoutineInitiated) {
        synchronized (isStopRoutineInitiatedLock) {
            this.isStopRoutineInitiated = isStopRoutineInitiated;
        }
    }

    public boolean isStopRoutineInitiated() {
        synchronized (isStopRoutineInitiatedLock) {
            return isStopRoutineInitiated;
        }
    }

    public void addMovedDistToFree(double dist) {
        movedDistToFree += dist;
    }

    public double getMovedDistToFree() {
        return movedDistToFree;
    }

    public void addDistToAlloc(int dist) {
        distToAlloc += dist;
    }

    public void setDestinationId(long destinationId) {
        synchronized (destinationLock) {
            this.destinationId = destinationId;
        }
    }

    public long getDestinationId() {
        synchronized (destinationLock) {
            return destinationId;
        }
    }
}
