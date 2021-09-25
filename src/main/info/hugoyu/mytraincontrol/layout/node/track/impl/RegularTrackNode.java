package info.hugoyu.mytraincontrol.layout.node.track.impl;

import com.google.common.collect.Range;
import info.hugoyu.mytraincontrol.exception.NodeAllocationException;
import info.hugoyu.mytraincontrol.layout.node.track.AbstractTrackNode;
import info.hugoyu.mytraincontrol.layout.BlockSectionResult;
import info.hugoyu.mytraincontrol.trainset.Trainset;

import java.util.HashMap;
import java.util.Map;

public class RegularTrackNode extends AbstractTrackNode implements Comparable<RegularTrackNode> {

    protected int length;

    protected Map<Integer, Range<Integer>> owners = new HashMap<>();
    private final Object ownersLock = new Object();

    public RegularTrackNode(long id0, Long id1, int length) {
        super(id0);

        this.length = length;

        if (id1 != null) {
            addConnection(id1, length);
        }
    }

    @Override
    public int compareTo(RegularTrackNode o) {
        return length - o.length;
    }

    @Override
    public BlockSectionResult alloc(Trainset trainset, int dist, Long nextNodeId, Long previousNodeId) throws NodeAllocationException {
        int trainsetAddress = trainset.getAddress();

        synchronized (ownersLock) {
            Range<Integer> ownedRange = owners.getOrDefault(trainsetAddress, Range.closedOpen(0, 0));

            int expectedUpperBound = ownedRange.upperEndpoint() + dist;
            int actualUpperBound = Math.min(length, expectedUpperBound);
            int allocatedDist = actualUpperBound - ownedRange.upperEndpoint();
            int remainingDist = expectedUpperBound - actualUpperBound;
            boolean isEntireSectionAllocated = actualUpperBound == length;

            Range<Integer> allocatingRange = Range.closedOpen(ownedRange.lowerEndpoint(), actualUpperBound);
            while (!isFree(trainset, allocatingRange)) {
                try {
                    ownersLock.wait();
                } catch (InterruptedException e) {

                }
            }
            owners.put(trainsetAddress, allocatingRange);

            return new BlockSectionResult(allocatedDist, remainingDist, isEntireSectionAllocated);
        }
    }

    @Override
    public BlockSectionResult free(Trainset trainset, int dist) throws NodeAllocationException {
        int trainsetAddress = trainset.getAddress();

        synchronized (ownersLock) {
            Range<Integer> ownedRange = owners.get(trainsetAddress);
            if (ownedRange == null) {
                throw new NodeAllocationException(NodeAllocationException.ExceptionType.FREEING_UNOWNED_SECTION,
                        trainset, this, dist);
            }

            int expectedLowerBound = ownedRange.lowerEndpoint() + dist;
            int actualLowerBound = Math.min(length, expectedLowerBound);
            int freedDist = actualLowerBound - ownedRange.lowerEndpoint();
            int remainingDist = expectedLowerBound - actualLowerBound;

            Range<Integer> freeingRange = Range.closedOpen(ownedRange.lowerEndpoint(), actualLowerBound);
            if (!ownedRange.encloses(freeingRange)) {
                throw new NodeAllocationException(NodeAllocationException.ExceptionType.FREEING_UNOWNED_SECTION,
                        trainset, this, dist);
            }

            boolean isEntireSectionFreed = false;
            Range<Integer> newOwnedRange = Range.closedOpen(actualLowerBound, ownedRange.upperEndpoint());
            if (newOwnedRange.isEmpty()) {
                owners.remove(trainsetAddress);
                isEntireSectionFreed = true;
            } else {
                owners.put(trainsetAddress, newOwnedRange);
            }

            ownersLock.notifyAll();

            return new BlockSectionResult(freedDist, remainingDist, isEntireSectionFreed);
        }
    }

    protected boolean isFree(Trainset trainset, Range<Integer> allocatingRange) {
        int trainsetAddress = trainset.getAddress();

        synchronized (ownersLock) {
            return owners.entrySet().stream()
                    .filter(entry -> entry.getKey() != trainsetAddress)
                    .allMatch(entry -> entry.getValue().intersection(allocatingRange).isEmpty());
        }
    }


}
