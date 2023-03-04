package info.hugoyu.mytraincontrol.util;

import com.google.common.collect.Range;
import info.hugoyu.mytraincontrol.exception.NodeAllocationException;
import info.hugoyu.mytraincontrol.layout.BlockSectionResult;
import info.hugoyu.mytraincontrol.layout.Vector;
import info.hugoyu.mytraincontrol.layout.node.Allocatable;
import info.hugoyu.mytraincontrol.layout.node.impl.StationTrackNode;
import info.hugoyu.mytraincontrol.trainset.Trainset;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

import static info.hugoyu.mytraincontrol.util.LayoutConstant.TRAIN_BUFFER_DISTANCE_TRAILING;

@Log4j2
public class AllocateUtil {

    private static final int ALLOCATE_AWAIT_TIMEOUT_MILLIS = 1000;

    /**
     * @param trainset
     * @param dist
     * @param nodesToAllocate
     * @param allocatedNodes
     * @return allocated distance - note that this number could be higher than the input dist, if any extra distance is allocated
     */
    public static int allocNode(Trainset trainset,
                                int dist,
                                List<Long> nodesToAllocate,
                                Set<Long> allocatedNodes) {
        log.debug("{} allocating {} with distance {}", trainset.getName(), nodesToAllocate, dist);

        List<NodeToAllocate> allocatingNodes = findAllocatingNodes(trainset, dist, new ArrayList<>(nodesToAllocate));
        final boolean isAllNodesAllocated = allocatingNodes.stream()
                .allMatch(allocatedNode -> allocate(allocatedNode, trainset));

        if (!isAllNodesAllocated) {
            revertAll(allocatingNodes, trainset);
            return 0;
        } else {
            int allocatedDist = 0;
            List<Future<Void>> isHardwareUpdated = new ArrayList<>();
            for (NodeToAllocate allocatingNode : allocatingNodes) {
                final Vector vector = allocatingNode.vector;
                final Allocatable allocatable = LayoutUtil.getNode(vector);
                final Range<Integer> newOwnedRange = allocatingNode.newOwnedRange;

                isHardwareUpdated.add(allocatable.updateHardware());

                allocatedNodes.add(vector.getId0());
                allocatedNodes.add(vector.getId1());
                allocatedDist += allocatingNode.getAllocatingDistance();

                boolean isEntireSectionAllocated = newOwnedRange.upperEndpoint() == allocatable.getSectionLength(vector);
                if (isEntireSectionAllocated) {
                    nodesToAllocate.remove(0);
                }
            }

            // wait for all hardware update to complete
            isHardwareUpdated.forEach(future -> {
                try {
                    future.get();
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException(e);
                }
            });

            return allocatedDist;
        }
    }

    private static boolean allocate(NodeToAllocate allocatingNode, Trainset trainset) {
        final Vector vector = allocatingNode.vector;
        final Allocatable allocatable = LayoutUtil.getNode(vector);
        final Lock occupierLock = allocatable.getOccupierLock();
        final Range<Integer> newOwnedRange = allocatingNode.newOwnedRange;

        log.debug("{} occupying {} with newRange {}", trainset.getName(), vector, newOwnedRange);

        occupierLock.lock();
        try {
            if (!allocatable.isFree(trainset, vector, newOwnedRange)) {
                try {
                    log.debug("{} waiting for {} at {} to become available", trainset.getName(), vector, newOwnedRange);
                    if (!allocatable.getOccupierChangeCondition()
                            .await(ALLOCATE_AWAIT_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)) {
                        return false;
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

            log.debug("{} allocated {} with new range {}", trainset.getName(), vector, newOwnedRange);
            allocatable.setOccupier(trainset, vector, newOwnedRange);
            return true;
        } finally {
            occupierLock.unlock();
        }
    }

    private static void revertAll(List<NodeToAllocate> allocatingNodes, Trainset trainset) {
        log.debug("{} revert all allocations", trainset.getName());
        allocatingNodes.forEach(allocatingNode -> {
            final Vector vector = allocatingNode.vector;
            final Allocatable allocatable = LayoutUtil.getNode(vector);
            final Range<Integer> prevOwnedRange = allocatingNode.prevOwnedRange;
            free(allocatable, vector, trainset, prevOwnedRange);
        });
    }

    @AllArgsConstructor
    private static class NodeToAllocate {
        private Vector vector;
        private Range<Integer> newOwnedRange;
        private Range<Integer> prevOwnedRange;

        public Integer getAllocatingDistance() {
            return newOwnedRange.upperEndpoint() - prevOwnedRange.upperEndpoint();
        }
    }

    /**
     * @param trainset
     * @param dist
     * @param nodesToAllocate
     * @return
     */
    private static List<NodeToAllocate> findAllocatingNodes(Trainset trainset,
                                                            int dist,
                                                            List<Long> nodesToAllocate) {
        if (dist <= 0 || nodesToAllocate.size() < 2) {
            return List.of();
        }

        final long id0 = nodesToAllocate.get(0);
        final long id1 = nodesToAllocate.get(1);
        final Vector vector = new Vector(id0, id1);

        log.debug("{} allocating {} with distance {}", trainset.getName(), vector, dist);

        final Allocatable allocatable = LayoutUtil.getNode(vector);

        final Lock occupierLock = allocatable.getOccupierLock();
        occupierLock.lock();
        try {
            final int sectionLength = allocatable.getSectionLength(vector);

            // if the current node is bidirectional, to make sure the train is able to move out of the bidirectional section
            // and not cause deadlock, will need to allocate at least
            //  1. the entire current node, plus
            //  2. part of next node:
            //      a. if next node is StationTrackNode, allocate trainset.length
            //      b. otherwise, allocate trainset.length + trailing buffer
            boolean isAllocatingDestinationTrackNode = nodesToAllocate.size() == 2 &&
                    allocatable instanceof StationTrackNode;
            boolean needAllocateExtraDist = !isAllocatingDestinationTrackNode && allocatable.isBidirectional();
            if (needAllocateExtraDist) {
                if (nodesToAllocate.size() < 3) {
                    return List.of();
                }

                final boolean isNextNodeStationTrackNode =
                        LayoutUtil.getNode(nodesToAllocate.get(1), nodesToAllocate.get(2)) instanceof StationTrackNode;

                int remainingSectionLength = sectionLength -
                        allocatable.getOccupiedRange(vector, trainset)
                                .map(Range::upperEndpoint)
                                .orElse(0);
                final int minAllocatingDist = remainingSectionLength +
                        trainset.getTotalLength() +
                        (!isNextNodeStationTrackNode ? TRAIN_BUFFER_DISTANCE_TRAILING : 0);

                if (isNextNodeStationTrackNode) {
                    dist = minAllocatingDist;
                } else {
                    dist = Math.max(dist, minAllocatingDist);
                }

                log.debug("{} allocating distance updated to {}", trainset.getName(), dist);
            }

            Range<Integer> ownedRange = allocatable.getOccupiedRange(vector, trainset)
                    .orElse(Range.closedOpen(0, 0));
            final int occupyingUpperBound = Math.min(sectionLength, ownedRange.upperEndpoint() + dist);
            Range<Integer> newOwnedRange = Range.closedOpen(ownedRange.lowerEndpoint(), occupyingUpperBound);
            NodeToAllocate nodeToAllocate = new NodeToAllocate(vector, newOwnedRange, ownedRange);
            final int remainingDist = dist - nodeToAllocate.getAllocatingDistance();

            List<NodeToAllocate> res = new ArrayList<>() {{
                add(nodeToAllocate);
            }};

            // allocate subsequent nodes if needed
            if (remainingDist > 0) {
                occupierLock.unlock();
                List<NodeToAllocate> nextAllocatingNodes =
                        findAllocatingNodes(trainset, remainingDist, nodesToAllocate.subList(1, nodesToAllocate.size()));
                occupierLock.lock();

                if (nextAllocatingNodes.isEmpty()) {
                    return List.of();
                } else {
                    res.addAll(nextAllocatingNodes);
                }
            }

            return res;
        } finally {
            occupierLock.unlock();
        }
    }

    public static BlockSectionResult freeNode(Trainset trainset,
                                              Vector vector,
                                              int dist) throws NodeAllocationException {
        final NodeAllocationException freeingUnownedSectionException = new NodeAllocationException(
                NodeAllocationException.ExceptionType.FREEING_UNOWNED_SECTION,
                trainset,
                vector,
                dist);
        final Allocatable allocatable = LayoutUtil.getNode(vector);
        final Lock occupierLock = allocatable.getOccupierLock();
        occupierLock.lock();
        try {
            final Range<Integer> occupiedRange = allocatable.getOccupiedRange(vector, trainset)
                    .orElseThrow(() -> freeingUnownedSectionException);
            int expectedLowerBound = occupiedRange.lowerEndpoint() + dist;
            int actualLowerBound = Math.min(allocatable.getSectionLength(vector), expectedLowerBound);
            int freedDist = actualLowerBound - occupiedRange.lowerEndpoint();
            int remainingDist = expectedLowerBound - actualLowerBound;

            Range<Integer> newOccupiedRange;
            try {
                newOccupiedRange = Range.closedOpen(actualLowerBound, occupiedRange.upperEndpoint());
            } catch (IllegalArgumentException e) { // invalid range
                throw freeingUnownedSectionException;
            }

            boolean isEntireSectionFreed = free(allocatable, vector, trainset, newOccupiedRange);

            return new BlockSectionResult(freedDist, remainingDist, isEntireSectionFreed);
        } finally {
            occupierLock.unlock();
        }
    }

    /**
     *
     * @return isEntireSectionFreed
     */
    private static boolean free(Allocatable allocatable, Vector vector, Trainset trainset, Range<Integer> newOccupiedRange) {
        final Lock occupierLock = allocatable.getOccupierLock();
        occupierLock.lock();
        try {
            boolean isEntireSectionFreed = false;
            if (newOccupiedRange.isEmpty()) {
                allocatable.removeOccupier(vector, trainset);
                isEntireSectionFreed = true;
            } else {
                allocatable.setOccupier(trainset, vector, newOccupiedRange);
            }
            allocatable.getOccupierChangeCondition().signalAll();
            return isEntireSectionFreed;
        } finally {
            occupierLock.unlock();
        }
    }

    public static void freeAllNodes(Vector vector, Trainset trainset) {
        final Allocatable allocatable = LayoutUtil.getNode(vector);
        final Lock occupierLock = allocatable.getOccupierLock();
        occupierLock.lock();
        try {
            allocatable.freeAll(trainset);
            allocatable.getOccupierChangeCondition().signalAll();
        } finally {
            occupierLock.unlock();
        }

    }

    public static boolean reserveStationTrack(StationTrackNode stationTrackNode, Trainset trainset) {
        if (!stationTrackNode.isPlatformTrackAbleToFit(trainset)) {
            return false;
        }

        trainset.freeAllNodes();

        try {
            stationTrackNode.occupyStationTrack(trainset);
            trainset.addAllocatedNodes(List.of(stationTrackNode.getId0(), stationTrackNode.getId1()));

            return true;
        } catch (NodeAllocationException e) {
            e.printStackTrace();
            return false;
        }
    }

}
