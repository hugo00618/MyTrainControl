package info.hugoyu.mytraincontrol.layout;

import info.hugoyu.mytraincontrol.exception.NodeAllocationException;
import info.hugoyu.mytraincontrol.layout.alias.Station;
import info.hugoyu.mytraincontrol.trainset.Trainset;
import info.hugoyu.mytraincontrol.util.LayoutUtil;
import lombok.extern.log4j.Log4j;

import java.util.List;

import static info.hugoyu.mytraincontrol.constant.LayoutConstants.TRAIN_BUFFER_DISTANCE;

@Log4j
public class MovingBlockManagerRunnable implements Runnable {

    private static final int INITIAL_MOVE_DISTANCE = 20;

    private Trainset trainset;

    private List<Long> nodesToAllocate, allocatedNodes;
    private Long previousNode;

    private double distToMove; // total distance to move

    private double movedDistToFree;
    private int allocatedMoveDist;

    public MovingBlockManagerRunnable(Trainset trainset, Route route) {
        this.trainset = trainset;
        this.allocatedNodes = trainset.getAllocatedNodes();
        this.nodesToAllocate = route.getNodes();
        this.distToMove = route.getCost();
    }

    @Override
    public void run() {
        trainset.resetMovedDist();

        try {
            // allocate initial buffer space
            allocate();

            while (distToMove > 0) {
                trainset.waitDistUpdate();

                double movedDist = trainset.resetMovedDist();
                log.debug("movedDist: " + movedDist);

                distToMove -= movedDist;

                allocatedMoveDist -= movedDist;
                movedDistToFree += movedDist;

                // free movedDistToFree
                free();

                // allocate distance if needed
                allocate();
            }
        } catch (NodeAllocationException e) {
            e.printStackTrace();
        }
    }

    private void allocate() throws NodeAllocationException {
        int minAllocateDist = getMinAllocateDistance();
        if (allocatedMoveDist < minAllocateDist) {
            int allocatedMovedDistBefore = allocatedMoveDist;
            while (allocatedMoveDist < minAllocateDist && !nodesToAllocate.isEmpty()) {
                long nodeId = nodesToAllocate.get(0);
                Long nextNode = nodesToAllocate.size() > 1 ? nodesToAllocate.get(1) : null;

                BlockSectionResult allocRes = LayoutUtil.allocNode(nodeId, trainset, 1, nextNode, previousNode);
                int distanceAllocated = allocRes.getConsumedDist();
                allocatedMoveDist += distanceAllocated;

                if (!allocatedNodes.contains(nodeId)) {
                    allocatedNodes.add(nodeId);
                }

                // remove from nodesToAllocate if the entire section has been allocated
                if (allocRes.isEntireSectionConsumed()) {
                    previousNode = nodesToAllocate.remove(0);
                    if (nodesToAllocate.isEmpty()) { // add inbound distance if the last node is an entry node for a station
                        Station station = LayoutUtil.getStation(previousNode);
                        if (station != null) {
                            // TODO: do this
//                    distToMove += stationNode.getInboundMoveDist(trainset);
                        }
                    }
                }

                trainset.addDistToMove(1);
            }
//            trainset.addDistToMove(allocatedMoveDist - allocatedMovedDistBefore);
        }
    }

    private void free() throws NodeAllocationException {
        while (movedDistToFree >= 1 && !allocatedNodes.isEmpty()) {
            long nodeId = allocatedNodes.get(0);

            int freeingDistance = (int) movedDistToFree;
            BlockSectionResult freeRes = LayoutUtil.freeNode(nodeId, trainset, freeingDistance);

            int distanceFreed = freeRes.getConsumedDist();
            movedDistToFree -= distanceFreed;

            log.debug("free'ed " + nodeId + " for distance " + distanceFreed);

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
