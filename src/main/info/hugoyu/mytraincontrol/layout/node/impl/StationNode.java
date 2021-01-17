package info.hugoyu.mytraincontrol.layout.node.impl;

import com.google.common.collect.Range;
import info.hugoyu.mytraincontrol.exception.NodeAllocationException;
import info.hugoyu.mytraincontrol.layout.node.AbstractGraphNode;
import info.hugoyu.mytraincontrol.layout.node.BlockSectionResult;
import info.hugoyu.mytraincontrol.trainset.Trainset;
import lombok.Getter;

@Getter
public class StationNode extends AbstractGraphNode {

    private int length;
    private long id0, id1;

    private volatile Trainset owner;
    private volatile Range<Long> allocatedRange;
    private final Object ownerLock = new Object();

    public StationNode(String id0, int length) {
        super(id0);

        this.length = length;
        this.id0 = Long.parseLong(id0);
        this.id1 = this.id0 + length;
        this.allocatedRange = Range.closedOpen(this.id0, this.id0);
    }

    @Override
    public BlockSectionResult alloc(Trainset trainset, int dist, String nextNodeId) {
        BlockSectionResult.BlockSectionResultBuilder res = BlockSectionResult.builder();
        synchronized (ownerLock) {
            while (owner != trainset && owner != null) {
                try {
                    ownerLock.wait();
                } catch (InterruptedException e) {

                }
            }

            owner = trainset;

            long expectedToId = allocatedRange.upperEndpoint() + dist;
            long toId = Math.min(id1, expectedToId);
            res.remainingDist((int) (expectedToId - toId));
            res.isSectionComplete(toId == id1);

            allocatedRange = Range.closedOpen(allocatedRange.lowerEndpoint(), toId);
        }
        return res.build();
    }

    /**
     * Only called by TrainUtil when initializing a trainset on a certain station track
     *
     * @param trainset
     * @return
     */
    public boolean reserve(Trainset trainset) {
        synchronized (ownerLock) {
            if (owner != null) {
                return false;
            }

            try {
                // allocate the section so the train stop in the center
                alloc(trainset, getInboundMoveDist(trainset), null);
                free(trainset, getInboundMargin(trainset));
            } catch (NodeAllocationException e) {

            }

            return true;
        }
    }

    public int getInboundMoveDist(Trainset trainset) {
        // divide the entire section into margin / trainLength / margin
        int trainLength = trainset.getProfile().getTotalLength();
        return trainLength + getInboundMargin(trainset);
    }

    private int getInboundMargin(Trainset trainset) {
        int trainLength = trainset.getProfile().getTotalLength();
        int margin = (length - trainLength) / 2;
        return margin;
    }

    @Override
    public BlockSectionResult free(Trainset trainset, int dist) throws NodeAllocationException {
        BlockSectionResult.BlockSectionResultBuilder res = BlockSectionResult.builder();
        synchronized (ownerLock) {
            if (owner != trainset) {
                throw new NodeAllocationException(NodeAllocationException.ExceptionType.FREEING_UNOWNED_SECTION,
                        trainset, this, dist);
            }

            long expectedToId = allocatedRange.lowerEndpoint() + dist;
            long toId = Math.min(id1, expectedToId);
            res.remainingDist((int) (expectedToId - toId));

            Range<Long> freeingRange = Range.closedOpen(allocatedRange.lowerEndpoint(), toId);
            if (!allocatedRange.encloses(freeingRange)) {
                throw new NodeAllocationException(NodeAllocationException.ExceptionType.FREEING_UNOWNED_SECTION,
                        trainset, this, dist);
            }

            allocatedRange = Range.closedOpen(toId, allocatedRange.upperEndpoint());

            if (allocatedRange.isEmpty()) {
                owner = null;
                allocatedRange = Range.closedOpen(this.id0, this.id0);
                res.isSectionComplete(true);
                ownerLock.notify();
            }
        }
        return res.build();
    }

}
