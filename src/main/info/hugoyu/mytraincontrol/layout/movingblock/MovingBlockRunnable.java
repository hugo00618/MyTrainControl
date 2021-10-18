package info.hugoyu.mytraincontrol.layout.movingblock;

import info.hugoyu.mytraincontrol.exception.NodeAllocationException;
import info.hugoyu.mytraincontrol.layout.BlockSectionResult;
import info.hugoyu.mytraincontrol.layout.Route;
import info.hugoyu.mytraincontrol.layout.alias.Station;
import info.hugoyu.mytraincontrol.layout.node.impl.StationTrackNode;
import info.hugoyu.mytraincontrol.trainset.Trainset;
import info.hugoyu.mytraincontrol.util.LayoutUtil;
import info.hugoyu.mytraincontrol.util.RouteUtil;
import lombok.extern.log4j.Log4j;

import java.util.List;

import static info.hugoyu.mytraincontrol.util.LayoutConstant.TRAIN_BUFFER_DISTANCE_HEADING;
import static info.hugoyu.mytraincontrol.util.LayoutConstant.TRAIN_BUFFER_DISTANCE_TRAILING;

@Log4j
public class MovingBlockRunnable implements Runnable {

    private static final int INITIAL_MOVE_DISTANCE = 20;

    private MovingBlockManager movingBlockManager;
    private Trainset trainset;
    private List<Long> nodesToAllocate;

    private Long previousNode;

    private boolean isBufferReleased;

    private Thread allocThread;

    public MovingBlockRunnable(MovingBlockManager movingBlockManager) {
        this.movingBlockManager = movingBlockManager;
        this.trainset = movingBlockManager.getTrainset();
        this.nodesToAllocate = movingBlockManager.getNodesToAllocate();
    }

    @Override
    public void run() {
        trainset.resetMovedDist();
        movingBlockManager.setIsStopRoutineInitiated(false);

        try {
            // allocate initial buffer space
            allocateInitialDistance();

            while (movingBlockManager.getDistToMove() >= 1) {
                double movedDist = trainset.resetMovedDist();
                if (movedDist > 0) {
                    double moveDistForBlockSection = movingBlockManager.logMovedDist(movedDist);

                    if (moveDistForBlockSection > 0) {
                        movingBlockManager.addAllocatedMoveDist(-moveDistForBlockSection);
                        movingBlockManager.addMovedDistToFree(moveDistForBlockSection);
                    }

                    // free movedDistToFree
                    free();

                    // allocate more distance if needed
                    allocate();
                } else {
                    trainset.waitDistUpdate();
                }
            }

            // free trailing buffer at the end of the journey
            movingBlockManager.addMovedDistToFree(TRAIN_BUFFER_DISTANCE_TRAILING);
            free();
        } catch (NodeAllocationException e) {
            throw new RuntimeException("Track allocation error");
        }
    }

    private void allocateInitialDistance() throws NodeAllocationException {
        movingBlockManager.addMovedDistToFree(-TRAIN_BUFFER_DISTANCE_TRAILING);
        allocate(TRAIN_BUFFER_DISTANCE_HEADING + INITIAL_MOVE_DISTANCE);
        isBufferReleased = false;
        trainset.addDistToMove(INITIAL_MOVE_DISTANCE);
    }

    private void allocate() {
        if (allocThread != null && allocThread.isAlive()) {
            return;
        }

        allocThread = new Thread(() -> {
            try {
                while (movingBlockManager.getAllocatedMoveDist() < getMinAllocateDistance()) {
                    int remainingDist = allocate(1);
                    if (remainingDist == 0) {
                        trainset.addDistToMove(1);
                    } else {
                        break;
                    }
                }

                // release heading buffer at the end of the trip
                if (movingBlockManager.getDistToAlloc() == 0 && !isBufferReleased) {
                    trainset.addDistToMove(TRAIN_BUFFER_DISTANCE_HEADING);
                    isBufferReleased = true;
                }
            } catch (NodeAllocationException e) {
                e.printStackTrace();
            }
        });
        allocThread.start();
    }

    /**
     * @param distance
     * @return remaining distance
     * @throws NodeAllocationException
     */
    private int allocate(int distance) throws NodeAllocationException {
        while (movingBlockManager.getDistToAlloc() > 0 && distance > 0 && nodesToAllocate.size() > 0) {
            long nodeId = nodesToAllocate.get(0);
            Long nextNode = nodesToAllocate.size() > 1 ? nodesToAllocate.get(1) : null;

            BlockSectionResult allocRes =
                    LayoutUtil.allocNode(nodeId, trainset, distance, nextNode, previousNode);
            int distanceAllocated = allocRes.getConsumedDist();
            distance -= distanceAllocated;
            movingBlockManager.addDistToAlloc(-distanceAllocated);
            movingBlockManager.addAllocatedMoveDist(distanceAllocated);

            if (trainset.getLastAllocatedNode() != nodeId) {
                trainset.addAllocatedNode(nodeId);
            }

            // remove from nodesToAllocate if the entire section has been allocated
            if (allocRes.isEntireSectionConsumed()) {
                previousNode = nodesToAllocate.remove(0);

                // if only one node remaining, perform stop routine if not done already
                if (nodesToAllocate.size() == 1 && !movingBlockManager.isStopRoutineInitiated()) {
                    initiateStopRoutine();
                }
            }
        }

        return distance;
    }

    private void free() throws NodeAllocationException {
        while (movingBlockManager.getDistToFree() > 0 && movingBlockManager.getMovedDistToFree() >= 1) {
            Long nodeId = trainset.getFirstAllocatedNode();
            if (nodeId == null) {
                throw new RuntimeException("Error freeing node");
            }

            BlockSectionResult freeRes = LayoutUtil.freeNode(nodeId, trainset, (int) movingBlockManager.getMovedDistToFree());

            int distanceFreed = freeRes.getConsumedDist();
            movingBlockManager.addDistToFree(-distanceFreed);
            movingBlockManager.addMovedDistToFree(-distanceFreed);

            // remove from allocatedNodes if the entire section has been freed
            if (freeRes.isEntireSectionConsumed()) {
                trainset.removeAllocatedNode(nodeId);
            }
        }
    }

    private int getMinAllocateDistance() {
        int minStoppingDist = (int) (Math.ceil(trainset.getCurrentMinimumStoppingDistance() + trainset.getCSpeed() * 0.5));
        int minAllocateDist = Math.max(minStoppingDist, INITIAL_MOVE_DISTANCE) + TRAIN_BUFFER_DISTANCE_HEADING;
        minAllocateDist = Math.min(minAllocateDist, (int) Math.ceil(movingBlockManager.getDistToMove()));
        return minAllocateDist;
    }

    private void initiateStopRoutine() {
        movingBlockManager.setIsStopRoutineInitiated(true);

        long entryNodeId = nodesToAllocate.get(0);
        Station station = LayoutUtil.getStation(entryNodeId);
        // todo: hang when no track available
        StationTrackNode stationTrackNode = station.findAvailableTrack(entryNodeId, false);
        Route inboundRoute = RouteUtil.findInboundRoute(entryNodeId, stationTrackNode);

        // replace entry node with inbound nodes
        nodesToAllocate.remove(0);
        nodesToAllocate.addAll(inboundRoute.getNodes());

        int inboundMoveDist = inboundRoute.getCost() + stationTrackNode.getInboundMoveDist(trainset);
        movingBlockManager.addDistToMove(inboundMoveDist);
        movingBlockManager.addDistToAlloc(inboundMoveDist);
        movingBlockManager.addDistToFree(inboundMoveDist);
    }

}
