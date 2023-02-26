package info.hugoyu.mytraincontrol.util;

import com.google.common.collect.Range;
import info.hugoyu.mytraincontrol.exception.NodeAllocationException;
import info.hugoyu.mytraincontrol.layout.BlockSectionResult;
import info.hugoyu.mytraincontrol.layout.Vector;
import info.hugoyu.mytraincontrol.layout.node.AbstractTrackNode;
import info.hugoyu.mytraincontrol.layout.node.Allocatable;
import info.hugoyu.mytraincontrol.layout.node.impl.StationTrackNode;
import info.hugoyu.mytraincontrol.trainset.Trainset;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static info.hugoyu.mytraincontrol.util.LayoutConstant.TRAIN_BUFFER_DISTANCE_TRAILING;

public class AllocateUtil {
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
                                List<Long> allocatedNodes) {
        if (dist <= 0) {
            return 0;
        }

        if (nodesToAllocate.size() < 2) {
            return 0;
        }

        final long id0 = nodesToAllocate.get(0);
        final long id1 = nodesToAllocate.get(1);
        final Vector vector = new Vector(id0, id1);

        Allocatable allocatable = LayoutUtil.getNode(vector);

        final Object occupierLock = allocatable.getOccupierLock();
        synchronized (occupierLock) {
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
                    return 0;
                }

                int remainingSectionLength = sectionLength -
                        allocatable.getOccupiedRange(vector, trainset)
                        .map(Range::upperEndpoint)
                        .orElse(0);
                int minAllocatingDist = remainingSectionLength + trainset.getTotalLength();

                AbstractTrackNode nextNode = LayoutUtil.getNode(nodesToAllocate.get(1), nodesToAllocate.get(2));
                if (nextNode instanceof StationTrackNode) {
                    dist = minAllocatingDist;
                } else {
                    minAllocatingDist += TRAIN_BUFFER_DISTANCE_TRAILING;
                    dist = Math.max(dist, minAllocatingDist);
                }
            }

            Range<Integer> ownedRange = allocatable.getOccupiedRange(vector, trainset)
                    .orElse(Range.closedOpen(0, 0));
            final int occupyingUpperBound = Math.min(sectionLength, ownedRange.upperEndpoint() + dist);
            Range<Integer> occupyingRange = Range.closedOpen(ownedRange.upperEndpoint(), occupyingUpperBound);
            Range<Integer> newOwnedRange = Range.closedOpen(ownedRange.lowerEndpoint(), occupyingRange.upperEndpoint());

            int allocatedDist = 0;
            final int allocatingDist = newOwnedRange.upperEndpoint() - ownedRange.upperEndpoint();
            final int remainingDist = dist - allocatingDist;

            // allocate subsequent nodes first if needed
            if (remainingDist > 0) {
                allocatedDist += allocNode(trainset, remainingDist, nodesToAllocate.subList(1, nodesToAllocate.size()), allocatedNodes);
                // unable to allocate (due to no enough nodes remaining, will need to initiate stop routine
                if (allocatedDist == 0) {
                    return 0;
                }
            }

            while (!allocatable.isFree(trainset, vector, occupyingRange)) {
                try {
                    occupierLock.wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

            allocatable.setOccupier(trainset, vector, newOwnedRange);
            Future<Void> isHardwareUpdated = allocatable.updateHardware();

            allocatedNodes.add(0, id1);
            allocatedNodes.add(0, id0);

            allocatedDist += allocatingDist;

            boolean isEntireSectionAllocated = newOwnedRange.upperEndpoint() == sectionLength;
            if (isEntireSectionAllocated) {
                nodesToAllocate.remove(0);
            }

            // wait for hardware update to complete
            try {
                isHardwareUpdated.get();
            } catch (ExecutionException | InterruptedException e) {

            }

            return allocatedDist;
        }
    }

    public static BlockSectionResult freeNode(Trainset trainset,
                                              Vector vector,
                                              int dist)
            throws NodeAllocationException {
        Allocatable allocatable = LayoutUtil.getNode(vector);
        final Object occupierLock = allocatable.getOccupierLock();
        synchronized (occupierLock) {
            Optional<Range<Integer>> maybeOccupiedRange = allocatable.getOccupiedRange(vector, trainset);
            if (maybeOccupiedRange.isEmpty()) {
                throw new NodeAllocationException(NodeAllocationException.ExceptionType.FREEING_UNOWNED_SECTION,
                        trainset, vector, dist);
            }

            final Range<Integer> occupiedRange = maybeOccupiedRange.get();
            int expectedLowerBound = occupiedRange.lowerEndpoint() + dist;
            int actualLowerBound = Math.min(allocatable.getSectionLength(vector), expectedLowerBound);
            int freedDist = actualLowerBound - occupiedRange.lowerEndpoint();
            int remainingDist = expectedLowerBound - actualLowerBound;

            Range<Integer> newOccupiedRange;
            try {
                newOccupiedRange = Range.closedOpen(actualLowerBound, occupiedRange.upperEndpoint());
            } catch (IllegalArgumentException e) { // invalid range
                throw new NodeAllocationException(NodeAllocationException.ExceptionType.FREEING_UNOWNED_SECTION,
                        trainset, vector, dist);
            }

            boolean isEntireSectionFreed = false;
            if (newOccupiedRange.isEmpty()) {
                allocatable.removeOccupier(vector, trainset);
                isEntireSectionFreed = true;
                occupierLock.notifyAll();
            } else {
                allocatable.setOccupiedRange(vector, trainset, newOccupiedRange);
            }

            return new BlockSectionResult(freedDist, remainingDist, isEntireSectionFreed);
        }
    }

    public static void freeAllNodes(Vector vector, Trainset trainset) {
        LayoutUtil.getNode(vector).freeAll(trainset);
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
