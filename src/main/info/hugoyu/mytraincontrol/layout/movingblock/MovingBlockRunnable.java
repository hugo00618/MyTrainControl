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
import lombok.extern.log4j.Log4j;

import java.util.ArrayList;
import java.util.List;

import static info.hugoyu.mytraincontrol.util.LayoutConstant.TRAIN_BUFFER_DISTANCE_HEADING;
import static info.hugoyu.mytraincontrol.util.LayoutConstant.TRAIN_BUFFER_DISTANCE_TRAILING;

@Log4j
public class MovingBlockRunnable implements Runnable {

    private static final int INITIAL_MOVE_DISTANCE = 20;

    private final MovingBlockManager movingBlockManager;
    private final Trainset trainset;
    private final List<Long> nodesToAllocate;
    private final boolean isUplink;

    private boolean isBufferReleased;

    private Thread allocThread;

    public MovingBlockRunnable(MovingBlockManager movingBlockManager) {
        this.movingBlockManager = movingBlockManager;
        this.trainset = movingBlockManager.getTrainset();
        this.nodesToAllocate = movingBlockManager.getNodesToAllocate();
        this.isUplink = movingBlockManager.isUplink();
    }

    @Override
    public void run() {
        trainset.resetMovedDist();
        movingBlockManager.setIsStopRoutineInitiated(false);

        try {
            // allocate initial buffer space
            allocateInitialDistance();

            while (movingBlockManager.getDistToMove() >= 1 || trainset.getDistToMove() >= 1) {
                double movedDist = trainset.resetMovedDist();
                if (movedDist > 0) {
                    double movedDistForBlockSection = movingBlockManager.logMovedDist(movedDist);

                    if (movedDistForBlockSection > 0) {
                        movingBlockManager.addAllocatedMoveDist(-movedDistForBlockSection);
                        movingBlockManager.addMovedDistToFree(movedDistForBlockSection);
                    }

                    // free movedDistToFree
                    free();

                    // allocate more distance if needed
                    allocate();
                } else {
                    trainset.waitDistUpdate();
                }
            }

            // free trailing buffer at the end of the trip
            movingBlockManager.addMovedDistToFree(TRAIN_BUFFER_DISTANCE_TRAILING);
            free();

            // todo: this is a temporary workaround to fix a "fail to free all nodes" issue
            AllocateUtil.reserveStationTrack(trainset.getAllocatedStationTrack(), trainset);
        } catch (NodeAllocationException e) {
            throw new RuntimeException("Track allocation error");
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
                while (movingBlockManager.getAllocatedMoveDist() < getMinAllocateDistance()) {
                    int allocatedDist = allocate(1);
                    if (allocatedDist > 0) {
                        trainset.addDistToMove(allocatedDist);
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
     * @return allocated distance
     * @throws NodeAllocationException
     */
    private int allocate(int distance) throws NodeAllocationException {
        int totalDistAllocated = 0;

        while (movingBlockManager.getDistToAlloc() > 0 && distance > 0 && nodesToAllocate.size() > 1) {
            List<Long> allocatedNodes = new ArrayList<>();
            final int distAllocated = AllocateUtil.allocNode(trainset, distance, nodesToAllocate, allocatedNodes);
            totalDistAllocated += distAllocated;

            distance -= distAllocated;
            movingBlockManager.addDistToAlloc(-distAllocated);
            movingBlockManager.addAllocatedMoveDist(distAllocated);

            trainset.addAllocatedNodes(allocatedNodes);

            // if only one node remaining, perform stop routine if not done already
            if ((distAllocated == 0 || nodesToAllocate.size() == 1) && !movingBlockManager.isStopRoutineInitiated()) {
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
        int minStoppingDist = (int) (Math.ceil(trainset.getCurrentMinimumStoppingDistance() + trainset.getCSpeed() * 0.5));
        int minAllocateDist = Math.max(minStoppingDist, INITIAL_MOVE_DISTANCE) + TRAIN_BUFFER_DISTANCE_HEADING;
        minAllocateDist = Math.min(minAllocateDist, (int) Math.ceil(movingBlockManager.getDistToMove()));
        return minAllocateDist;
    }

    private void initiateStopRoutine() {
        movingBlockManager.setIsStopRoutineInitiated(true);

        long entryNodeId = nodesToAllocate.get(0);
        Route inboundRoute = RouteUtil.findRouteToAvailableStationTrack(
                trainset,
                entryNodeId,
                isUplink,
                false,
                true);
        StationTrackNode stationTrackNode = LayoutUtil.getStationTrackNode(inboundRoute.getDestinationVector());

        // replace entry node with inbound nodes
        nodesToAllocate.remove(0);
        nodesToAllocate.addAll(inboundRoute.getNodes());

        int inboundMoveDist = inboundRoute.getCost();
        int stationInboundDist = stationTrackNode.getInboundMoveDist(trainset);
        if (inboundRoute.isUplink() == stationTrackNode.isUplink()) {
            inboundMoveDist += stationInboundDist;
        } else {
            inboundMoveDist -= stationInboundDist;
        }

        movingBlockManager.addDistToMove(inboundMoveDist);
        movingBlockManager.addDistToAlloc(inboundMoveDist);
        movingBlockManager.addDistToFree(inboundMoveDist);
        movingBlockManager.setDestinationId(inboundRoute.getDestinationVector().getId1());
    }

}
