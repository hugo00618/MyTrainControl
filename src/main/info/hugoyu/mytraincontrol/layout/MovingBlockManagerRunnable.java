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

    private int freedDist;

    public MovingBlockManagerRunnable(Trainset trainset, Route route) {
        this.trainset = trainset;

        this.allocatedNodes = trainset.getAllocatedNodes();
        this.nodesToAllocate = route.getNodes();
        this.distToMove = route.getCost();

        printDebugMessage();
    }

    @Override
    public void run() {
        trainset.resetMovedDist();

        try {
            // allocate initial buffer space
            allocate();
//                allocate(TRAIN_BUFFER_DISTANCE, true);
//                allocate(TRAIN_BUFFER_DISTANCE + INITIAL_MOVE_DISTANCE, false);

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
                // todo: fix bug - if this blocks, it still holds trainset's distLock
                allocate();
//                    int minStoppingDist = (int) (Math.ceil(trainset.getCurrentMinimumStoppingDistance()) + trainset.getCSpeed() * 0.5);
//                    int minAllocateDist = minStoppingDist + TRAIN_BUFFER_DISTANCE;
//
//                    log.debug("minAllocateDist: " + minAllocateDist);
//                    log.debug("allocatedMoveDist: " + allocatedMoveDist);
//
//                    if (allocatedMoveDist < minAllocateDist) {
//                        allocate(minAllocateDist, false);
//                    }
            }
        } catch (NodeAllocationException e) {
            e.printStackTrace();
        }
    }

    private void allocate() throws NodeAllocationException {
        int minStoppingDist = (int) (Math.ceil(trainset.getCurrentMinimumStoppingDistance() + trainset.getCSpeed() * 0.3));
        int minAllocateDist = Math.max(minStoppingDist, INITIAL_MOVE_DISTANCE) + TRAIN_BUFFER_DISTANCE;

        if (allocatedMoveDist < minAllocateDist) {
            int allocatedMovedDistBefore = allocatedMoveDist;
            while (allocatedMoveDist < minAllocateDist && !nodesToAllocate.isEmpty()) {
                long nodeId = nodesToAllocate.get(0);
                Long nextNode = nodesToAllocate.size() > 1 ? nodesToAllocate.get(1) : null;

                BlockSectionResult allocRes = LayoutUtil.allocNode(nodeId, trainset, minAllocateDist - allocatedMoveDist, nextNode, previousNode);
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
            }
            trainset.addDistToMove(allocatedMoveDist - allocatedMovedDistBefore);
        }
    }

//    private void allocate(int minAllocateDist, boolean isBufferDistance) throws NodeAllocationException {
//        int totalAllocatedDist = 0;
//        while (allocatedMoveDist < minAllocateDist && !nodesToAllocate.isEmpty()) {
//            long nodeId = nodesToAllocate.get(0);
//            Long nextNode = nodesToAllocate.size() > 1 ? nodesToAllocate.get(1) : null;
//
//            int allocatingDist = minAllocateDist - allocatedMoveDist;
//            BlockSectionResult allocRes = LayoutUtil.allocNode(nodeId, trainset, allocatingDist, nextNode, previousNode);
//
//            int distanceAllocated = allocRes.getConsumedDist();
//            allocatedMoveDist += distanceAllocated;
//            totalAllocatedDist += distanceAllocated;
//
//            log.debug("alloc'ed " + nodeId + " for distance " + distanceAllocated);
//
//            if (!allocatedNodes.contains(nodeId)) {
//                allocatedNodes.add(nodeId);
//            }
//
//            // remove from nodesToAllocate if the entire section has been allocated
//            boolean isEntireSectionAllocated = allocatingDist > distanceAllocated;
//            if (isEntireSectionAllocated) {
//                previousNode = nodesToAllocate.remove(0);
//                if (nodesToAllocate.isEmpty()) { // add inbound distance if the last node is an entry node for a station
//                    Station station = LayoutUtil.getStation(previousNode);
//                    if (station != null) {
//                        // TODO: do this
////                    distToMove += stationNode.getInboundMoveDist(trainset);
//                    }
//                }
//            }
//        }
//
//        if (!isBufferDistance) {
//            trainset.addDistToMove(totalAllocatedDist);
//        }
//    }

    private void free() throws NodeAllocationException {
        while (movedDistToFree >= 1 && !allocatedNodes.isEmpty()) {
            long nodeId = allocatedNodes.get(0);

            int freeingDistance = (int) movedDistToFree;
            BlockSectionResult freeRes = LayoutUtil.freeNode(nodeId, trainset, freeingDistance);

            int distanceFreed = freeRes.getConsumedDist();
            movedDistToFree -= distanceFreed;

            log.debug("free'ed " + nodeId + " for distance " + distanceFreed);
            freedDist += distanceFreed;

            allocatedMoveDist -= distanceFreed;

            // remove from allocatedNodes if the entire section has been freed
            if (freeRes.isEntireSectionConsumed()) {
                allocatedNodes.remove(nodeId);
            }
        }
    }

    private void printDebugMessage() {
        StringBuilder sb = new StringBuilder();
        for (long node : nodesToAllocate) {
            sb.append(node).append(" ");
        }
        log.debug(trainset.getName() + ": nodes to allocate: " + sb);
    }
}
