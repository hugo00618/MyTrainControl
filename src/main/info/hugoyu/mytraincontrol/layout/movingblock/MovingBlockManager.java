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

    private double allocatedMoveDist;
    private double distToMove; // total distance to move
    private double calibrationOffset;
    private final Lock distToMoveLock = new ReentrantLock(true);

    private int distToAlloc; // total distance to alloc

    private int distToFree; // total distance to free
    private final Object distToFreeLock = new Object();

    private double movedDistToFree;

    public MovingBlockManager(Trainset trainset) {
        this.trainset = trainset;
    }

    public void prepareToMove(Route route) {
        this.nodesToAllocate = route.getNodes();
        this.destinationId = nodesToAllocate.get(nodesToAllocate.size() - 1);

        int distToMove = calcDistToMove(trainset, route);
        this.distToMove += distToMove;
        this.distToAlloc += distToMove;
        this.distToFree += distToMove;
    }

    public Runnable getNewMovingBlockRunnable() {
        return new MovingBlockRunnable(this);
    }

    public void calibrate(long nodeId, int sensorPosition, SensorState sensorState) {
        Route remainingRoute = RouteUtil.findRoute(nodeId, destinationId);
        List<Long> remainingNodes = remainingRoute.getNodes();
        if (remainingNodes.size() == 1 && LayoutUtil.isStationTrackNode(remainingNodes.get(0))) {
            int calibratedDistToMove = calcCalibratedDistToMove(
                    trainset,
                    LayoutUtil.getStationTrackNode(remainingNodes.get(0)),
                    sensorPosition,
                    sensorState);
            calibrateDistToMove(calibratedDistToMove);
        }
    }

    private int calcCalibratedDistToMove(Trainset trainset, StationTrackNode node, int sensorPosition, SensorState sensorState) {
        int inboundMoveDist = node.getInboundMoveDist(trainset);
        int calibratedDistToMove = inboundMoveDist - sensorPosition;
        if (sensorState == EXIT) {
            calibratedDistToMove -= trainset.getTotalLength();
        }
        return calibratedDistToMove;
    }

    private int calcDistToMove(Trainset trainset, Route route) {
        StationTrackNode stationTrackNode = LayoutUtil.getStationTrackNode(nodesToAllocate.get(0));
        int outboundDist = stationTrackNode.getOutboundMoveDist(trainset);
        return outboundDist + route.getMinMoveDist();
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

    public int getDistToFree() {
        synchronized (distToFreeLock) {
            return distToFree;
        }
    }

    private void calibrateDistToMove(double calibratedDistToMove) {
        distToMoveLock.lock();
        try {
            System.out.println("currentDistToMove: " + distToMove);
            System.out.println("calibratedDistToMove: " + calibratedDistToMove);

            double offset = calibratedDistToMove - distToMove;
            calibrationOffset += offset;
            distToMove = calibratedDistToMove;
            trainset.addDistToMove(offset);
        } finally {
            distToMoveLock.unlock();
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

    public void addDistToFree(int dist) {
        synchronized (distToFreeLock) {
            distToFree += dist;
        }
    }

    public void addMovedDistToFree(double dist) {
        movedDistToFree += dist;
    }

    public void addDistToAlloc(int dist) {
        distToAlloc += dist;
    }
}
