package info.hugoyu.mytraincontrol.layout.movingblock;

import info.hugoyu.mytraincontrol.layout.Route;
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

    public void calibrate(long nodeId, int sensorPosition, SensorState sensorState) {
        Route remainingRoute = RouteUtil.findRoute(nodeId, getDestinationId(), isUplink);
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

    private int calcCalibratedDistToMove(Trainset trainset, Route remainingRoute, int sensorPosition, SensorState sensorState) {
        int calibratedDistToMove = remainingRoute.getCost() - sensorPosition;
        if (isStopRoutineInitiated) {
            StationTrackNode stationTrackNode = LayoutUtil.getStationTrackNode(remainingRoute.getDestinationNode());
            calibratedDistToMove += stationTrackNode.getInboundMoveDist(trainset);
        }
        if (sensorState == EXIT) {
            calibratedDistToMove -= trainset.getTotalLength();
        }

        return calibratedDistToMove;
    }

    private int calcDistToMove(Trainset trainset, Route route) {
        StationTrackNode stationTrackNode = LayoutUtil.getStationTrackNode(nodesToAllocate.get(0));
        return route.getCost() - stationTrackNode.getInboundMoveDist(trainset);
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
