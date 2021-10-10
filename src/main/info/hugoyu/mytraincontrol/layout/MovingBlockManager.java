package info.hugoyu.mytraincontrol.layout;

import info.hugoyu.mytraincontrol.layout.node.impl.StationTrackNode;
import info.hugoyu.mytraincontrol.trainset.Trainset;
import info.hugoyu.mytraincontrol.util.LayoutUtil;
import lombok.Getter;

import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Getter
public class MovingBlockManager {

    private Trainset trainset;

    private List<Long> nodesToAllocate;

    private double allocatedMoveDist;
    private double distToMove; // total distance to move
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

        int distToMove = calcDistToMove(trainset, route);
        this.distToMove += distToMove;
        this.distToAlloc += distToMove;
        this.distToFree += distToMove;
    }

    public Runnable getMovingBlockRunnable() {
        return new MovingBlockRunnable(this);
    }

    private int calcDistToMove(Trainset trainset, Route route) {
        StationTrackNode stationTrackNode = LayoutUtil.getStationTrackNode(nodesToAllocate.get(0));
        int outboundDist = stationTrackNode.getOutboundMoveDist(trainset);
        return outboundDist + route.getMoveDist();
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
