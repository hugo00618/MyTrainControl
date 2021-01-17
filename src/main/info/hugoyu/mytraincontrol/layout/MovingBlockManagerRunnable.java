package info.hugoyu.mytraincontrol.layout;

import info.hugoyu.mytraincontrol.constant.LayoutConstants;
import info.hugoyu.mytraincontrol.exception.NodeAllocationException;
import info.hugoyu.mytraincontrol.layout.node.BlockSectionResult;
import info.hugoyu.mytraincontrol.trainset.Trainset;
import info.hugoyu.mytraincontrol.util.LayoutUtil;
import lombok.extern.log4j.Log4j;

import java.util.LinkedHashSet;
import java.util.List;

@Log4j
public class MovingBlockManagerRunnable implements Runnable {

    private Trainset trainset;
    private Route route;

    private List<String> nodesToAllocate;
    private LinkedHashSet<String> allocatedNodes;

    private double distToMove; // total distance to move
    private double lastUpdatedDistToMove; // last updated trainset's distToMove
    private int allocatedStoppingDist;

    // dist's are double whereas block allocation is integer, preserving remainder
    private double allocRemainder;
    private double freeRemainder;

    public MovingBlockManagerRunnable(Trainset trainset, Route route) {
        this.trainset = trainset;
        this.route = route;

        this.allocatedNodes = trainset.getAllocatedNodes();
        this.nodesToAllocate = route.getRouteNodes();

        StringBuilder nodes = new StringBuilder();
        for (String node : nodesToAllocate) {
            nodes.append(node).append(" ");
        }
        log.debug(trainset.getName() + ": nodes to allocate: " + nodes);
        this.distToMove = route.getCost();
    }

    @Override
    public void run() {
        allocRemainder = 0;
        freeRemainder = 0;

        // allocate initial buffer space
        try {
            allocate(LayoutConstants.TRAIN_BUFFER_DISTANCE + 20);
        } catch (NodeAllocationException e) {
            e.printStackTrace();
        }

        trainset.setDistToMove(20);
        lastUpdatedDistToMove = 20;

        try {
            synchronized (trainset.distLock) {
                while (distToMove > 0) {
                    trainset.waitDistUpdate();

                    double currentDistToMove = trainset.getDistToMove();
                    double movedDist = lastUpdatedDistToMove - currentDistToMove;
                    lastUpdatedDistToMove = currentDistToMove;

                    distToMove -= movedDist;

                    // free movedDist
                    free(movedDist);

                    // allocate distance if needed
                    double minStoppingDist = trainset.getCurrentMinimumStoppingDistance() + trainset.getCSpeed() * 0.3;
                    double minAllocateDist = minStoppingDist + LayoutConstants.TRAIN_BUFFER_DISTANCE;
                    if (allocatedStoppingDist < minAllocateDist) {
                        allocate(minAllocateDist - allocatedStoppingDist);
                        lastUpdatedDistToMove = allocatedStoppingDist;
                        trainset.updateDistToMove(allocatedStoppingDist);
                        log.debug(trainset.getName() + ": updating distToMove to: " + allocatedStoppingDist);
                    }

                }
            }
        } catch (NodeAllocationException e) {
            e.printStackTrace();
        }
    }

    private void allocate(double distance) throws NodeAllocationException {
        distance -= allocRemainder;
        int distToAlloc = (int) Math.ceil(distance);
        allocRemainder = distToAlloc - distance;

        while (distToAlloc > 0 && !nodesToAllocate.isEmpty()) {
            String nodeId = nodesToAllocate.get(0);
            String nextNodeId = nodesToAllocate.size() > 1 ? nodesToAllocate.get(1) : null;


            BlockSectionResult allocRes = LayoutUtil.allocNode(nodeId, trainset, distToAlloc, nextNodeId);
            int distToAllocBefore = distToAlloc;
            distToAlloc = allocRes.getRemainingDist();
            int distanceAllocated = distToAllocBefore - distToAlloc;
            allocatedStoppingDist += distanceAllocated;

            allocatedNodes.add(nodeId);

            // remove from nodesToAllocate if entire section has been allocated
            if (allocRes.isSectionComplete()) {
                String removedNodeId = nodesToAllocate.remove(0);
//                if (nodesToAllocate.isEmpty()) { // allocated last node (station node), add inbound distance
//                    StationNode stationNode = (StationNode) LayoutUtil.getLayoutNode(removedNodeId);
//                    distToMove += stationNode.getInboundMoveDist(trainset);
            }

            log.debug(trainset.getName() + ": allocated node " + nodeId + " for distance " + distanceAllocated);
        }
    }

    private void free(double distance) throws NodeAllocationException {
        distance += freeRemainder;
        int distToFree = (int) (distance);
        freeRemainder = distance - distToFree;

        while (distToFree > 0 && !allocatedNodes.isEmpty()) {
            String nodeId = allocatedNodes.iterator().next();


            BlockSectionResult freeRes = LayoutUtil.freeNode(nodeId, trainset, distToFree);
            int distToFreeBefore = distToFree;
            distToFree = freeRes.getRemainingDist();
            int distanceFreed = distToFreeBefore - distToFree;
            allocatedStoppingDist -= distanceFreed;

            // remove from allocatedNodes if entire section has benn freed
            if (freeRes.isSectionComplete()) {
                allocatedNodes.remove(nodeId);
            }

            log.debug(trainset.getName() + ": freed node " + nodeId + " for distance " + distanceFreed);
        }
    }
}
