package info.hugoyu.mytraincontrol.layout.node.impl;

import com.google.common.collect.Range;
import info.hugoyu.mytraincontrol.exception.NodeAllocationException;
import info.hugoyu.mytraincontrol.layout.node.AbstractGraphNode;
import info.hugoyu.mytraincontrol.layout.node.BlockSectionResult;
import info.hugoyu.mytraincontrol.trainset.Trainset;

import java.util.HashMap;
import java.util.Map;

public class RegularNode extends AbstractGraphNode {

    private long id0, id1;

    private Map<Trainset, Range<Long>> owners = new HashMap<>();
    private final Object ownersLock = new Object();

    public RegularNode(long id0, long id1) {
        super(String.valueOf(id0), String.valueOf(id1), (int) (id1 - id0));

        this.id0 = id0;
        this.id1 = id1;
    }

    @Override
    public BlockSectionResult alloc(Trainset trainset, int dist, String nextNodeId) {
        BlockSectionResult.BlockSectionResultBuilder res = BlockSectionResult.builder();
        synchronized (ownersLock) {
            if (!owners.containsKey(trainset)) {
                owners.put(trainset, Range.closedOpen(id0, id0));
            }

            Range<Long> ownedRange = owners.get(trainset);

            long expectedToId = ownedRange.upperEndpoint() + dist;
            long toId = Math.min(id1, expectedToId);
            res.remainingDist((int) (expectedToId - toId));
            res.isSectionComplete(toId == id1);

            Range<Long> allocatingRange = Range.closedOpen(ownedRange.lowerEndpoint(), toId);
            while (!isFree(trainset, allocatingRange)) {
                try {
                    ownersLock.wait();
                } catch (InterruptedException e) {

                }
            }

            owners.put(trainset, allocatingRange);
        }
        return res.build();
    }

    @Override
    public BlockSectionResult free(Trainset trainset, int dist) throws NodeAllocationException {
        BlockSectionResult.BlockSectionResultBuilder res = BlockSectionResult.builder();
        synchronized (ownersLock) {
            Range<Long> ownedRange = owners.get(trainset);
            if (ownedRange == null) {
                throw new NodeAllocationException(NodeAllocationException.ExceptionType.FREEING_UNOWNED_SECTION,
                        trainset, this, dist);
            }

            long expectedToId = ownedRange.lowerEndpoint() + dist;
            long toId = Math.min(id1, expectedToId);
            res.remainingDist((int) (expectedToId - toId));

            Range<Long> freeingRange = Range.closedOpen(ownedRange.lowerEndpoint(), toId);
            if (!ownedRange.encloses(freeingRange)) {
                throw new NodeAllocationException(NodeAllocationException.ExceptionType.FREEING_UNOWNED_SECTION,
                        trainset, this, dist);
            }

            Range<Long> newOwnedRange = Range.closedOpen(toId, ownedRange.upperEndpoint());
            if (newOwnedRange.isEmpty()) {
                owners.remove(trainset);
                res.isSectionComplete(true);
            } else {
                owners.put(trainset, newOwnedRange);
            }


            ownersLock.notifyAll();
        }
        return res.build();
    }

    private boolean isFree(Trainset trainset, Range<Long> allocatingRange) {
        synchronized (ownersLock) {
            return owners.entrySet().stream()
                    .filter(entry -> entry.getKey() != trainset)
                    .allMatch(entry -> entry.getValue().intersection(allocatingRange).isEmpty());
        }
    }


}
