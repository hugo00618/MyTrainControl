package info.hugoyu.mytraincontrol.layout.node.impl;

import com.google.common.collect.Range;
import com.google.gson.annotations.SerializedName;
import info.hugoyu.mytraincontrol.json.layout.TurnoutJson;
import info.hugoyu.mytraincontrol.layout.Connection;
import info.hugoyu.mytraincontrol.layout.Vector;
import info.hugoyu.mytraincontrol.layout.node.AbstractTrackNode;
import info.hugoyu.mytraincontrol.switchable.AbstractSwitchable;
import info.hugoyu.mytraincontrol.trainset.Trainset;
import info.hugoyu.mytraincontrol.util.SwitchUtil;
import lombok.Getter;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.function.Consumer;

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
    private AbstractSwitchable turnout;

    private final boolean isUplink;


    private static final int NO_OCCUPIER = -1;
    private int occupier;

    private Vector occupiedVector;
    private Range<Integer> occupiedRange;

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
                       boolean isUplink,
                       AbstractSwitchable turnout) {
        super(true);

        this.id = id;
        this.idClosed = idClosed;
        this.idThrown = idThrown;
        this.distClosed = distClosed;
        this.distThrown = distThrown;
        this.type = type;
        this.address = address;
        this.isUplink = isUplink;
        this.turnout = turnout;

        this.occupier = NO_OCCUPIER;
    }

    public TurnoutNode(TurnoutJson turnoutJson, boolean isUplink, AbstractSwitchable turnout) {
        this(turnoutJson.getId(),
                turnoutJson.getIdClosed(),
                turnoutJson.getIdThrown(),
                turnoutJson.getDistClosed(),
                turnoutJson.getDistThrown(),
                turnoutJson.getType(),
                turnoutJson.getAddress(),
                isUplink,
                turnout);
    }

    @Override
    public List<Connection> getConnections() {
        switch (type) {
            case MERGE:
                return List.of(
                        new Connection(idClosed, id, distClosed, isUplink, true),
                        new Connection(idThrown, id, distThrown, isUplink, true));
            case DIVERGE:
                return List.of(
                        new Connection(id, idClosed, distClosed, isUplink, true),
                        new Connection(id, idThrown, distThrown, isUplink, true));
            default:
                throw new RuntimeException("Unsupported turnout type");
        }
    }

    @Override
    public boolean isFree(Trainset trainset, Vector vector, Range<Integer> range) {
        occupierLock.lock();
        try {
            // return true if there is no occupier or if the current occupier is trainset itself
            return occupier == NO_OCCUPIER || occupier == trainset.getAddress();
        } finally {
            occupierLock.unlock();
        }
    }

    @Override
    public void setOccupier(Trainset trainset, Vector vector, Range<Integer> range) {
        occupierLock.lock();
        try {
            occupier = trainset.getAddress();
            occupiedVector = vector;
            occupiedRange = range;
        } finally {
            occupierLock.unlock();
        }
    }

    @Override
    public Future<Void> updateHardware() {
        occupierLock.lock();
        try {
            CompletableFuture<Void> isHardwareUpdated = new CompletableFuture<>();
            Consumer<Long> callback = actualExecutionTime -> isHardwareUpdated.complete(null);

            if (isThrown(occupiedVector)) {
                SwitchUtil.setSwitchState(turnout, AbstractSwitchable.State.THROWN, callback);
            } else {
                SwitchUtil.setSwitchState(turnout, AbstractSwitchable.State.CLOSED, callback);
            }

            return isHardwareUpdated;
        } finally {
            occupierLock.unlock();
        }
    }

    @Override
    public int getSectionLength(Vector vector) {
        return isThrown(vector) ? distThrown : distClosed;
    }

    @Override
    public Optional<Range<Integer>> getOccupiedRange(Vector vector, Trainset trainset) {
        occupierLock.lock();
        try {
            return getOccupiedRangeImmediately(vector, trainset);
        } finally {
            occupierLock.unlock();
        }
    }

    @Override
    public Optional<Range<Integer>> getOccupiedRangeImmediately(Vector vector, Trainset trainset) {
        return Optional.ofNullable(occupier == trainset.getAddress() ? occupiedRange : null);
    }

    @Override
    public void removeOccupier(Vector vector, Trainset trainset) {
        this.removeOccupier();
    }

    private boolean isThrown(Vector vector) {
        return vector.getId0() == idThrown || vector.getId1() == idThrown;
    }

    @Override
    public void freeAll(Trainset trainset) {
        this.removeOccupier();
    }

    private void removeOccupier() {
        occupierLock.lock();
        try {
            occupier = NO_OCCUPIER;
            occupiedRange = null;
        } finally {
            occupierLock.unlock();
        }
    }

}
