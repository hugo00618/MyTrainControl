package info.hugoyu.mytraincontrol.util;

import com.google.common.collect.Range;
import info.hugoyu.mytraincontrol.exception.NodeAllocationException;
import info.hugoyu.mytraincontrol.layout.BlockSectionResult;
import info.hugoyu.mytraincontrol.layout.Vector;
import info.hugoyu.mytraincontrol.layout.node.AbstractTrackNode;
import info.hugoyu.mytraincontrol.layout.node.Allocatable;
import info.hugoyu.mytraincontrol.layout.node.impl.StationTrackNode;
import info.hugoyu.mytraincontrol.trainset.Trainset;
import lombok.extern.log4j.Log4j2;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.locks.Lock;

import static info.hugoyu.mytraincontrol.util.LayoutConstant.TRAIN_BUFFER_DISTANCE_TRAILING;

@Log4j2
public class AllocateUtil {
    // todo: fix deadlock under the following situation:
    // train 1 and 2 are in the same station both departing from the same direction
    // the outbound route for this station is node A (bi-direct), B (single-direct), C (single-direct)
    // train 1 allocates node A and B and starts travelling first
    // train 2 is significantly longer than train 1 and thus needs to allocate A, B and C
    // train 2 successfully allocates C and then waits to allocate B which is held by train 1
    // train 1 continues to move to the point where it needs node C, which is held by train 2
    // a deadlock occurs
    //
    // solution: add a timer on await() with backoff

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

                log.debug("{} allocating distance updated to {}", trainset.getName(), dist);
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
                occupierLock.unlock();
                allocatedDist += allocNode(trainset, remainingDist, nodesToAllocate.subList(1, nodesToAllocate.size()), allocatedNodes);
                occupierLock.lock();
                // unable to allocate (due to no enough nodes remaining, will need to initiate stop routine
                if (allocatedDist == 0) {
                    return 0;
                }
            }

            log.debug("{} occupying {} with range {}",
                    trainset.getName(), vector, occupyingRange);
            while (!allocatable.isFree(trainset, vector, occupyingRange)) {
                try {
                    log.debug("{} waiting for {} at {} to become available",
                            trainset.getName(), vector, occupyingRange);
                    allocatable.getOccupierChangeCondition().await();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

            allocatable.setOccupier(trainset, vector, newOwnedRange);
            Future<Void> isHardwareUpdated = allocatable.updateHardware();

            log.debug("{} allocated {} with new range {}", trainset.getName(), vector, newOwnedRange);

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
                throw new RuntimeException(e);
            }

            return allocatedDist;
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

            boolean isEntireSectionFreed = false;
            if (newOccupiedRange.isEmpty()) {
                allocatable.removeOccupier(vector, trainset);
                isEntireSectionFreed = true;
            } else {
                allocatable.setOccupiedRange(vector, trainset, newOccupiedRange);
            }
            allocatable.getOccupierChangeCondition().signalAll();

            return new BlockSectionResult(freedDist, remainingDist, isEntireSectionFreed);
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
