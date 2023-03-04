package info.hugoyu.mytraincontrol.layout.movingblock;

import info.hugoyu.mytraincontrol.exception.NodeAllocationException;
import info.hugoyu.mytraincontrol.layout.BlockSectionResult;
import info.hugoyu.mytraincontrol.layout.Route;
import info.hugoyu.mytraincontrol.layout.Vector;
import info.hugoyu.mytraincontrol.layout.node.impl.StationTrackNode;
import info.hugoyu.mytraincontrol.trainset.Trainset;
import info.hugoyu.mytraincontrol.util.AllocateUtil;
import info.hugoyu.mytraincontrol.util.LayoutUtil;
import info.hugoyu.mytraincontrol.util.RouteUtil;
import lombok.extern.log4j.Log4j2;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static info.hugoyu.mytraincontrol.util.LayoutConstant.TRAIN_BUFFER_DISTANCE_HEADING;
import static info.hugoyu.mytraincontrol.util.LayoutConstant.TRAIN_BUFFER_DISTANCE_TRAILING;

@Log4j2
public class MovingBlockRunnable implements Runnable {

    private static final int INITIAL_MOVE_DISTANCE = 20;

    private final MovingBlockManager movingBlockManager;
    private final Trainset trainset;
    private final List<Long> nodesToAllocate;

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

            while ((nodesToAllocate.size() > 1 && movingBlockManager.getDistToAlloc() >= 1) ||
                    trainset.getDistToMove() >= 1) {

                // allocate more distance if needed
                allocate();

                double movedDist = trainset.resetMovedDist();
                if (movedDist > 0) {
                    double movedDistForBlockSection = movingBlockManager.logMovedDist(movedDist);

                    if (movedDistForBlockSection > 0) {
                        movingBlockManager.addAllocatedMoveDist(-movedDistForBlockSection);
                        movingBlockManager.addMovedDistToFree(movedDistForBlockSection);
                    }

                    // free movedDistToFree
                    free();
                } else {
                    trainset.waitDistUpdate();
                }
            }

            // free trailing buffer at the end of the trip
            movingBlockManager.addMovedDistToFree(TRAIN_BUFFER_DISTANCE_TRAILING);
            free();

            // todo: this is a temporary workaround to fix a "fail to free all nodes" issue
            trainset.resetAllocatedNodes();
        } catch (NodeAllocationException e) {
            log.error("{} NodeAllocationException ", trainset.getName(), e);
            throw new RuntimeException("Track allocation error", e);
        }
    }

    private void allocateInitialDistance() throws NodeAllocationException {
        movingBlockManager.addMovedDistToFree(-TRAIN_BUFFER_DISTANCE_TRAILING);
        int allocatedDist = allocate(TRAIN_BUFFER_DISTANCE_HEADING + INITIAL_MOVE_DISTANCE);
        isBufferReleased = false;
        trainset.addDistToMove(allocatedDist - TRAIN_BUFFER_DISTANCE_HEADING);
    }

    private void allocate() {
        if (allocThread != null && allocThread.isAlive()) {
            return;
        }

        allocThread = new Thread(() -> {
            try {
                int allocatedDistToMove = 0;
                while (movingBlockManager.getAllocatedMoveDist() < getMinAllocateDistance()) {
                    int allocatedDist = allocate(1);
                    allocatedDistToMove += allocatedDist;
                    if (allocatedDist == 0) {
                        break;
                    }
                }
                trainset.addDistToMove(allocatedDistToMove);

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
     * @return allocated distance
     * @throws NodeAllocationException
     */
    private int allocate(int distance) throws NodeAllocationException {
        int totalDistAllocated = 0;

        while (movingBlockManager.getDistToAlloc() > 0 && distance > 0 && nodesToAllocate.size() > 1) {
            Set<Long> allocatedNodes = new HashSet<>();
            final int distAllocated = AllocateUtil.allocNode(trainset, distance, nodesToAllocate, allocatedNodes);
            totalDistAllocated += distAllocated;

            distance -= distAllocated;
            movingBlockManager.addDistToAlloc(-distAllocated);
            movingBlockManager.addAllocatedMoveDist(distAllocated);

            trainset.addAllocatedNodes(allocatedNodes);

            // if only one node remaining, perform stop routine if not done already
            if ((distAllocated == 0 || nodesToAllocate.size() == 2) && !movingBlockManager.isStopRoutineInitiated()) {
                initiateStopRoutine();
            }
        }

        return totalDistAllocated;
    }

    private void free() throws NodeAllocationException {
        while (movingBlockManager.getDistToFree() > 0 && movingBlockManager.getMovedDistToFree() >= 1) {
            Vector vectorToFree = trainset.getFirstAllocatedVector();

            BlockSectionResult freeRes = AllocateUtil.freeNode(
                    trainset,
                    vectorToFree,
                    (int) movingBlockManager.getMovedDistToFree());

            int distanceFreed = freeRes.getConsumedDist();
            movingBlockManager.addDistToFree(-distanceFreed);
            movingBlockManager.addMovedDistToFree(-distanceFreed);

            // remove from allocatedNodes if the entire section has been freed
            if (freeRes.isEntireSectionConsumed()) {
                trainset.removeFirstAllocatedNode();
            }
        }
    }

    private int getMinAllocateDistance() {
        final int minTravellingDist = Math.max(
                INITIAL_MOVE_DISTANCE,
                (int) Math.ceil(trainset.getCurrentMinimumStoppingDistance() +
                        trainset.getCSpeed() * 0.5)); // allocating 0.5 sec of travelling distance for next cycle
        final int minAllocateDist = Math.min(
                minTravellingDist + TRAIN_BUFFER_DISTANCE_HEADING,
                (int) Math.ceil(movingBlockManager.getTotalDistToMove()));
        return minAllocateDist;
    }

    private void initiateStopRoutine() {
        movingBlockManager.setIsStopRoutineInitiated(true);

        long entryNodeId = nodesToAllocate.get(nodesToAllocate.size() - 1);
        Route inboundRoute = RouteUtil.findRouteToAvailableStationTrack(
                trainset,
                entryNodeId,
                movingBlockManager.isUplink(),
                false,
                true);
        StationTrackNode stationTrackNode = LayoutUtil.getStationTrackNode(inboundRoute.getDestinationVector());

        // replace entry node with inbound nodes
        nodesToAllocate.remove(nodesToAllocate.size() - 1);
        nodesToAllocate.addAll(inboundRoute.getNodes());

        int inboundMoveDist = inboundRoute.getCost() - stationTrackNode.getOutboundMoveDist(trainset);
        movingBlockManager.addTotalDistToMove(inboundMoveDist);
        movingBlockManager.addDistToAlloc(inboundMoveDist);
        movingBlockManager.addDistToFree(inboundMoveDist);
        movingBlockManager.setDestinationId(inboundRoute.getDestinationVector().getId1());
    }

}
