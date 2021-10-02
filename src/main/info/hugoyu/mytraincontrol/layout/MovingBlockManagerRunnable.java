package info.hugoyu.mytraincontrol.layout;

import info.hugoyu.mytraincontrol.exception.NodeAllocationException;
import info.hugoyu.mytraincontrol.layout.alias.Station;
import info.hugoyu.mytraincontrol.trainset.Trainset;
import info.hugoyu.mytraincontrol.util.LayoutUtil;
import lombok.extern.log4j.Log4j;

import java.util.List;

import static info.hugoyu.mytraincontrol.util.LayoutConstant.TRAIN_BUFFER_DISTANCE;

@Log4j
public class MovingBlockManagerRunnable implements Runnable {

    private static final int INITIAL_MOVE_DISTANCE = 20;

    private Trainset trainset;

    private List<Long> nodesToAllocate, allocatedNodes;
    private Long previousNode;

    private double distToMove; // total distance to move
    private double distToAlloc; // total distance to alloc

    private double movedDistToFree;
    private int allocatedMoveDist;

    private boolean isBufferReleased;

    public MovingBlockManagerRunnable(Trainset trainset, Route route) {
        this.trainset = trainset;
        this.allocatedNodes = trainset.getAllocatedNodes();
        this.nodesToAllocate = route.getNodes();
        this.distToMove = route.getCost();
        this.distToAlloc = route.getCost();
    }

    @Override
    public void run() {
        trainset.resetMovedDist();

        try {
            // allocate initial buffer space
            allocateInitialDistance();

            while (distToMove > 0) {
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
        while (distToAlloc > 0 && distance > 0 && !nodesToAllocate.isEmpty()) {
            long nodeId = nodesToAllocate.get(0);
            Long nextNode = nodesToAllocate.size() > 1 ? nodesToAllocate.get(1) : null;

            BlockSectionResult allocRes = LayoutUtil.allocNode(nodeId, trainset, distance, nextNode, previousNode);
            int distanceAllocated = allocRes.getConsumedDist();
            distance -= distanceAllocated;
            distToAlloc -= distanceAllocated;
            allocatedMoveDist += distanceAllocated;

            if (!allocatedNodes.contains(nodeId)) {
                allocatedNodes.add(nodeId);
            }

            // remove from nodesToAllocate if the entire section has been allocated
            if (allocRes.isEntireSectionConsumed()) {
                previousNode = nodesToAllocate.remove(0);

                // if the last remaining node is an entry node for a station,
                // find an available track and perform stopping routine
                if (nodesToAllocate.size() == 1) {
                    Station station = LayoutUtil.getStation(previousNode);
                    if (station != null) {
                        // TODO: do this
//                    distToMove += stationNode.getInboundMoveDist(trainset);
                    }
                }
            }
        }

        return distance;
    }

    private void free() throws NodeAllocationException {
        while (movedDistToFree >= 1 && !allocatedNodes.isEmpty()) {
            long nodeId = allocatedNodes.get(0);

            int freeingDistance = (int) movedDistToFree;
            BlockSectionResult freeRes = LayoutUtil.freeNode(nodeId, trainset, freeingDistance);

            int distanceFreed = freeRes.getConsumedDist();
            movedDistToFree -= distanceFreed;
            allocatedMoveDist -= distanceFreed;

            // remove from allocatedNodes if the entire section has been freed
            if (freeRes.isEntireSectionConsumed()) {
                allocatedNodes.remove(nodeId);
            }
        }
    }

    private int getMinAllocateDistance() {
        int minStoppingDist = (int) (Math.ceil(trainset.getCurrentMinimumStoppingDistance() + trainset.getCSpeed() * 0.3));
        int minAllocateDist = Math.max(minStoppingDist, INITIAL_MOVE_DISTANCE) + TRAIN_BUFFER_DISTANCE;
        minAllocateDist = Math.min(minAllocateDist, (int) Math.ceil(distToMove));
        return minAllocateDist;
    }
}
