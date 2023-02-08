package info.hugoyu.mytraincontrol.layout.movingblock;

import com.google.common.annotations.VisibleForTesting;
import info.hugoyu.mytraincontrol.layout.Route;
import info.hugoyu.mytraincontrol.layout.Vector;
import info.hugoyu.mytraincontrol.layout.node.impl.RegularTrackNode;
import info.hugoyu.mytraincontrol.layout.node.impl.StationTrackNode;
import info.hugoyu.mytraincontrol.sensor.SensorState;
import info.hugoyu.mytraincontrol.trainset.Trainset;
import info.hugoyu.mytraincontrol.util.LayoutUtil;
import info.hugoyu.mytraincontrol.util.RouteUtil;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

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
    private final Object distanceLock = new Object();

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
        List<Long> nodesToAllocate = new ArrayList<>(route.getNodes());
        nodesToAllocate.add(0, trainset.getAllocatedStationTrack().get().getNodeIds(route.isUplink()).get(0));
        this.nodesToAllocate = nodesToAllocate;
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

    public void calibrate(Vector nodeVector, int sensorOffset, SensorState sensorState) {
        double offset;
        synchronized (distanceLock) {
            int calibratedDistToMove = calcCalibratedDistToMove(trainset, nodeVector, sensorOffset, sensorState);
            offset = calibratedDistToMove - distToMove;
            calibrationOffset += offset;
            distToMove = calibratedDistToMove;
        }
        trainset.addDistToMove(offset);
    }

    @VisibleForTesting
    public int calcCalibratedDistToMove(Trainset trainset, Vector nodeVector, int sensorOffset, SensorState sensorState) {
        RegularTrackNode node = (RegularTrackNode) LayoutUtil.getNode(nodeVector);
        Route remainingRoute = RouteUtil.findRoute(nodeVector.getId0(), getDestinationId(), isUplink);
        // remainingRoute.getCost() is the length between sensor's reference node to the destination node
        int calibratedDistToMove = remainingRoute.getCost();

        // if the train's travelling direction is the same with node's,
        // then we need to subtract offset from the total cost, otherwise we add
        if (isUplink == node.isUplink()) {
            calibratedDistToMove -= sensorOffset;
        } else {
            calibratedDistToMove += sensorOffset;
        }

        if (isStopRoutineInitiated) {
            StationTrackNode stationTrackNode = LayoutUtil.getStationTrackNode(remainingRoute.getDestinationVector());
            calibratedDistToMove -= stationTrackNode.getOutboundMoveDist(trainset);
        }
        if (sensorState == EXIT) {
            calibratedDistToMove -= trainset.getTotalLength();
        }

        return calibratedDistToMove;
    }

    private int calcDistToMove(Trainset trainset, Route route) {
        StationTrackNode stationTrackNode = trainset.getAllocatedStationTrack().get();
        return stationTrackNode.getOutboundMoveDist(trainset) + route.getCost();
    }

    public void addDistToMove(double dist) {
        synchronized (distanceLock) {
            distToMove += dist;
        }
    }

    public double getDistToMove() {
        synchronized (distanceLock) {
            return distToMove;
        }
    }

    public double getAllocatedMoveDist() {
        synchronized (distanceLock) {
            return allocatedMoveDist;
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
        synchronized (distanceLock) {
            allocatedMoveDist += dist;
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
        synchronized (distanceLock) {
            distToMove -= movedDist;

            if (calibrationOffset >= movedDist) {
                calibrationOffset -= movedDist;
                return 0;
            } else {
                double res = movedDist - calibrationOffset;
                calibrationOffset = 0;
                return res;
            }
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
