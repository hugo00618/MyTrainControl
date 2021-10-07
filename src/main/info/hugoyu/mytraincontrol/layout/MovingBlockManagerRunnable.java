package info.hugoyu.mytraincontrol.layout;

import com.google.common.annotations.VisibleForTesting;
import info.hugoyu.mytraincontrol.exception.NodeAllocationException;
import info.hugoyu.mytraincontrol.layout.alias.Station;
import info.hugoyu.mytraincontrol.layout.node.impl.StationTrackNode;
import info.hugoyu.mytraincontrol.trainset.Trainset;
import info.hugoyu.mytraincontrol.util.LayoutUtil;
import info.hugoyu.mytraincontrol.util.RouteUtil;
import lombok.extern.log4j.Log4j;

import java.util.List;

import static info.hugoyu.mytraincontrol.util.LayoutConstant.TRAIN_BUFFER_DISTANCE;

@Log4j
public class MovingBlockManagerRunnable implements Runnable {

    private static final int INITIAL_MOVE_DISTANCE = 20;

    private Trainset trainset;

    private List<Long> nodesToAllocate;
    private Long previousNode;

    private double distToMove; // total distance to move
    private int distToAlloc; // total distance to alloc
    private int distToFree; // total distance to free

    private double movedDistToFree;
    private double allocatedMoveDist;

    private boolean isBufferReleased;
    private boolean isStopRoutineInitiated;

    public MovingBlockManagerRunnable(Trainset trainset) {
        this.trainset = trainset;
    }

    public void prepareToMove(Route route) {
        this.nodesToAllocate = route.getNodes();

        int distToMove = calcDistToMove(trainset, route);
        this.distToMove += distToMove;
        this.distToAlloc += distToMove;
        this.distToFree += distToMove;
    }

    @Override
    public void run() {
        trainset.resetMovedDist();
        isStopRoutineInitiated = false;

        try {
            // allocate initial buffer space
            allocateInitialDistance();

            while (distToMove >= 1) {
                double movedDist = trainset.resetMovedDist();
                if (movedDist > 0) {
                    distToMove -= movedDist;
                    allocatedMoveDist -= movedDist;
                    movedDistToFree += movedDist;

                    // free movedDistToFree
                    free();

                    // allocate more distance if needed
                    allocate();
                } else {
                    trainset.waitDistUpdate();
                }
            }
        } catch (NodeAllocationException e) {
            e.printStackTrace();
        }
    }

    private void allocateInitialDistance() throws NodeAllocationException {
        allocate(TRAIN_BUFFER_DISTANCE + INITIAL_MOVE_DISTANCE);
        isBufferReleased = false;
        trainset.addDistToMove(INITIAL_MOVE_DISTANCE);
    }

    private void allocate() throws NodeAllocationException {
        while (allocatedMoveDist < getMinAllocateDistance()) {
            int remainingDist = allocate(1);
            if (remainingDist == 0) {
                trainset.addDistToMove(1);
            } else {
                break;
            }
        }

        if (distToAlloc == 0 && !isBufferReleased) {
            trainset.addDistToMove(TRAIN_BUFFER_DISTANCE);
            isBufferReleased = true;
        }
    }

    /**
     * @param distance
     * @return remaining distance
     * @throws NodeAllocationException
     */
    private int allocate(int distance) throws NodeAllocationException {
        while (distToAlloc > 0 && distance > 0 && nodesToAllocate.size() > 1) {
            long nodeId = nodesToAllocate.get(0);
            long nextNode = nodesToAllocate.get(1);

            BlockSectionResult allocRes = LayoutUtil.allocNode(nodeId, trainset, distance, nextNode, previousNode);
            int distanceAllocated = allocRes.getConsumedDist();
            distance -= distanceAllocated;
            distToAlloc -= distanceAllocated;
            allocatedMoveDist += distanceAllocated;

            trainset.addAllocatedNode(nodeId);

            // remove from nodesToAllocate if the entire section has been allocated
            if (allocRes.isEntireSectionConsumed()) {
                previousNode = nodesToAllocate.remove(0);

                // if only one node remaining, perform stop routine if not done already
                if (nodesToAllocate.size() == 1 && !isStopRoutineInitiated) {
                   initiateStopRoutine();
                }
            }
        }

        return distance;
    }

    private void free() throws NodeAllocationException {
        while (distToFree > 0 && movedDistToFree >= 1) {
            Long nodeId = trainset.getFirstAllocatedNode();
            if (nodeId == null) {
                throw new RuntimeException("Error freeing node");
            }

            BlockSectionResult freeRes = LayoutUtil.freeNode(nodeId, trainset, (int) movedDistToFree);

            int distanceFreed = freeRes.getConsumedDist();
            distToFree -= distanceFreed;
            movedDistToFree -= distanceFreed;

            // remove from allocatedNodes if the entire section has been freed
            if (freeRes.isEntireSectionConsumed()) {
                trainset.removeAllocatedNode(nodeId);
            }
        }
    }

    private int getMinAllocateDistance() {
        int minStoppingDist = (int) (Math.ceil(trainset.getCurrentMinimumStoppingDistance() + trainset.getCSpeed() * 0.3));
        int minAllocateDist = Math.max(minStoppingDist, INITIAL_MOVE_DISTANCE) + TRAIN_BUFFER_DISTANCE;
        minAllocateDist = Math.min(minAllocateDist, (int) Math.ceil(distToMove));
        return minAllocateDist;
    }

    private void initiateStopRoutine() {
        isStopRoutineInitiated = true;

        long entryNodeId = nodesToAllocate.get(0);
        Station station = LayoutUtil.getStation(entryNodeId);
        StationTrackNode stationTrackNode = station.findAvailableTrack(entryNodeId, false);
        Route inboundRoute = RouteUtil.findInboundRoute(entryNodeId, stationTrackNode);

        // replace entry node with inbound nodes
        nodesToAllocate.remove(0);
        nodesToAllocate.addAll(inboundRoute.getNodes());

        int inboundMoveDist = stationTrackNode.getInboundMoveDist(trainset);
        distToMove += inboundMoveDist;
        distToAlloc += inboundMoveDist;
        distToFree += inboundMoveDist;
    }

    private int calcDistToMove(Trainset trainset, Route route) {
        StationTrackNode stationTrackNode = LayoutUtil.getStationTrackNode(nodesToAllocate.get(0));
        int outboundDist = stationTrackNode.getOutboundMoveDist(trainset);
        return outboundDist + route.getMoveDist();
    }

    @VisibleForTesting
    double getDistToMove() {
        return distToMove;
    }
}
