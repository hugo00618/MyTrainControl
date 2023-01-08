package info.hugoyu.mytraincontrol.layout.node.impl;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Range;
import com.google.gson.annotations.SerializedName;
import info.hugoyu.mytraincontrol.exception.InvalidIdException;
import info.hugoyu.mytraincontrol.exception.NodeAllocationException;
import info.hugoyu.mytraincontrol.json.layout.TurnoutJson;
import info.hugoyu.mytraincontrol.layout.BlockSectionResult;
import info.hugoyu.mytraincontrol.layout.node.AbstractTrackNode;
import info.hugoyu.mytraincontrol.layout.node.Connection;
import info.hugoyu.mytraincontrol.switchable.impl.Turnout;
import info.hugoyu.mytraincontrol.trainset.Trainset;
import info.hugoyu.mytraincontrol.util.SwitchUtil;
import lombok.Getter;

import java.util.List;
import java.util.Map;

import static info.hugoyu.mytraincontrol.layout.node.impl.TurnoutNode.Type.DIVERGE;

public class TurnoutNode extends AbstractTrackNode {

    public enum Type {
        @SerializedName("DIVERGE")
        DIVERGE("diverge"),

        @SerializedName("MERGE")
        MERGE("merge");

        final String type;

        Type(String type) {
            this.type = type;
        }
    }

    @Getter
    private final long id, idClosed, idThrown;

    private final int distClosed, distThrown;

    @Getter
    private Type type;

    private int address;
    private Turnout turnout;

    private final boolean isUplink, isBidirectional;

    private Integer owner;
    private Range<Integer> ownedRange;
    private int length;
    private final Object ownerLock = new Object();

    /**
     * @param id         id of the turnout node
     * @param idClosed   closed (main) line destination node id
     * @param idThrown   thrown (diverging) line destination node id
     * @param distClosed distance for closed line
     * @param distThrown distance for thrown line
     * @param type       turnout type
     * @param address    turnout address
     * @param isUplink   whether turnout is in uplink
     */
    public TurnoutNode(long id, long idClosed, long idThrown,
                       int distClosed, int distThrown, Type type, int address,
                       boolean isUplink, boolean isBidirectional,
                       Turnout turnout) {
        super();

        this.id = id;
        this.idClosed = idClosed;
        this.idThrown = idThrown;
        this.distClosed = distClosed;
        this.distThrown = distThrown;
        this.type = type;
        this.address = address;
        this.isUplink = isUplink;
        this.isBidirectional = isBidirectional;
        this.turnout = turnout;
    }

    public TurnoutNode(TurnoutJson turnoutJson, boolean isUplink, Turnout turnout) {
        this(turnoutJson.getId(),
                turnoutJson.getIdClosed(),
                turnoutJson.getIdThrown(),
                turnoutJson.getDistClosed(),
                turnoutJson.getDistThrown(),
                turnoutJson.getType(),
                turnoutJson.getAddress(),
                isUplink,
                turnoutJson.isBidirectional(),
                turnout);
    }

    @Override
    public List<Connection> getConnections() {
        switch (type) {
            case MERGE:
                return List.of(
                        new Connection(idClosed, id, distClosed, isUplink, isBidirectional),
                        new Connection(idThrown, id, distThrown, isUplink, isBidirectional));
            case DIVERGE:
                return List.of(
                        new Connection(id, idClosed, distClosed, isUplink, isBidirectional),
                        new Connection(id, idThrown, distThrown, isUplink, isBidirectional));
            default:
                throw new RuntimeException("Unsupported turnout type");
        }
    }

    @Override
    public List<Long> getIds() {
        switch (type) {
            case MERGE:
                return List.of(idClosed, idThrown);
            case DIVERGE:
                return List.of(id);
            default:
                throw new RuntimeException("Unsupported turnout type");
        }
    }

    @Override
    public BlockSectionResult alloc(Trainset trainset, int dist, Long nextNodeId, Long previousNodeId) throws
            NodeAllocationException {
        synchronized (ownerLock) {
            final int trainsetAddress = trainset.getAddress();
            if (owner == null || owner != trainsetAddress) { // if trainset is not the current owner

                // wait if occupied
                while (owner != null) {
                    try {
                        ownerLock.wait();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }

                owner = trainsetAddress;
                ownedRange = Range.closedOpen(0, 0);

                long referenceNode = type == DIVERGE ? nextNodeId : previousNodeId;
                if (referenceNode == idClosed) {
                    length = distClosed;
                    SwitchUtil.setSwitchState(turnout, Turnout.State.CLOSED, false);
                } else if (referenceNode == idThrown) {
                    length = distThrown;
                    SwitchUtil.setSwitchState(turnout, Turnout.State.THROWN, false);
                } else {
                    throw new InvalidIdException(referenceNode, InvalidIdException.Type.NOT_FOUND);
                }
            }

            int expectedUpperBound = ownedRange.upperEndpoint() + dist;
            int actualUpperBound = Math.min(length, expectedUpperBound);
            int allocatedDist = actualUpperBound - ownedRange.upperEndpoint();
            int remainingDist = expectedUpperBound - actualUpperBound;
            boolean isEntireSectionAllocated = actualUpperBound == length;

            ownedRange = Range.closedOpen(ownedRange.lowerEndpoint(), actualUpperBound);

            return new BlockSectionResult(allocatedDist, remainingDist, isEntireSectionAllocated);
        }
    }

    @Override
    public BlockSectionResult free(Trainset trainset, int dist) throws NodeAllocationException {
        synchronized (ownerLock) {
            int trainsetAddress = trainset.getAddress();
            if (owner == null || owner != trainsetAddress || ownedRange == null) {
                throw new NodeAllocationException(NodeAllocationException.ExceptionType.FREEING_UNOWNED_SECTION,
                        trainset, this, dist);
            }

            int expectedLowerBound = ownedRange.lowerEndpoint() + dist;
            int actualLowerBound = Math.min(length, expectedLowerBound);
            int freedDist = actualLowerBound - ownedRange.lowerEndpoint();
            int remainingDist = expectedLowerBound - actualLowerBound;

            Range<Integer> newOwnedRange;
            try {
                newOwnedRange = Range.closedOpen(actualLowerBound, ownedRange.upperEndpoint());
            } catch (IllegalArgumentException e) { // invalid range
                throw new NodeAllocationException(NodeAllocationException.ExceptionType.FREEING_UNOWNED_SECTION,
                        trainset, this, dist);
            }

            boolean isEntireSectionFreed = false;
            if (newOwnedRange.isEmpty()) {
                owner = null;
                ownedRange = null;
                isEntireSectionFreed = true;
                ownerLock.notifyAll();
            } else {
                ownedRange = newOwnedRange;
            }

            return new BlockSectionResult(freedDist, remainingDist, isEntireSectionFreed);
        }
    }

    @Override
    public void freeAll(Trainset trainset) throws NodeAllocationException {
        synchronized (ownerLock) {
            int trainsetAddress = trainset.getAddress();
            if (owner == null || owner != trainsetAddress || ownedRange == null) {
                throw new NodeAllocationException(NodeAllocationException.ExceptionType.FREEING_UNOWNED_SECTION,
                        trainset, this, 0);
            }

            owner = null;
            ownedRange = null;
            ownerLock.notifyAll();
        }
    }

    @Override
    public String getOwnerStatus(int ownerId) {
        if (owner != null && owner == ownerId) {
            return ownedRange.toString();
        }
        throw new RuntimeException(String.format("%d does not own node %d", ownerId, id));
    }

    @Override
    public Map<Integer, String> getOwnerSummary() {
        if (owner != null) {
            return ImmutableMap.of(owner, ownedRange.toString());
        }
        return ImmutableMap.of();
    }
}
