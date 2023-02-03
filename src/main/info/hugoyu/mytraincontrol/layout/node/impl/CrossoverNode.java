package info.hugoyu.mytraincontrol.layout.node.impl;

import com.google.common.collect.Range;
import info.hugoyu.mytraincontrol.json.layout.CrossoverJson;
import info.hugoyu.mytraincontrol.layout.Connection;
import info.hugoyu.mytraincontrol.layout.Vector;
import info.hugoyu.mytraincontrol.layout.node.AbstractTrackNode;
import info.hugoyu.mytraincontrol.switchable.Switchable;
import info.hugoyu.mytraincontrol.switchable.impl.Crossover;
import info.hugoyu.mytraincontrol.trainset.Trainset;
import info.hugoyu.mytraincontrol.util.SwitchUtil;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class CrossoverNode extends AbstractTrackNode {

    private final int length, crossLength;

    private final Crossover crossover;

    private final Connection uplinkStraightConnection, downlinkStraightConnection;
    private final List<Connection> crossConnections;

    // (vector -> (owner -> ownedRange))
    private final Map<Vector, AbstractMap.SimpleImmutableEntry<Integer, Range<Integer>>>
            occupiers = new HashMap<>();
    private final Object occupierLock = new Object();

    protected CrossoverNode(int length, int crossLength,
                            Connection uplinkStraightConnection,
                            Connection downlinkStraightConnection,
                            List<Connection> crossConnections,
                            Crossover crossover
    ) {
        super(true);

        this.length = length;
        this.crossLength = crossLength;

        this.crossover = crossover;

        this.uplinkStraightConnection = uplinkStraightConnection;
        this.downlinkStraightConnection = downlinkStraightConnection;
        this.crossConnections = crossConnections;
    }

    public CrossoverNode(CrossoverJson crossoverJson,
                         Connection uplinkStraightConnection,
                         Connection downlinkStraightConnection,
                         List<Connection> crossConnections,
                         Crossover crossover) {
        this(
                crossoverJson.getLength(),
                crossoverJson.getCrossLength(),
                uplinkStraightConnection,
                downlinkStraightConnection,
                crossConnections,
                crossover
        );
    }

    @Override
    public Object getOccupierLock() {
        return occupierLock;
    }

    @Override
    public boolean isFree(Trainset trainset, Vector vector, Range<Integer> range) {
        synchronized (occupierLock) {
            // no occupiers, return true
            if (occupiers.isEmpty()) {
                return true;
            }

            // when there's one occupier, return true only when both occupied and requesting connections are
            // straight connections so that they don't interfere with each other
            if (occupiers.size() == 1) {
                Map.Entry<Vector, AbstractMap.SimpleImmutableEntry<Integer, Range<Integer>>> occupierEntry =
                        occupiers.entrySet().iterator().next();
                Vector occupiedVector = occupierEntry.getKey();
                int currentOccupier = occupierEntry.getValue().getKey();

                if (occupiedVector.equals(vector)) {
                    // if requesting vector is the same as the occupied vector, return true if trainset is the occupier
                    return currentOccupier == trainset.getAddress();
                } else {
                    // if requesting vector is not the same as the occupied vector, return true if both vectors are straight connection
                    return isStraightConnection(occupiedVector) && isStraightConnection(vector);
                }
            }

            // crossover cannot hold more than 2 occupiers
            return false;
        }
    }

    @Override
    public void setOccupier(Trainset trainset, Vector vector, Range<Integer> range) {
        synchronized (occupierLock) {
            occupiers.put(vector, new AbstractMap.SimpleImmutableEntry<>(trainset.getAddress(), range));


        }
    }

    @Override
    public void updateHardware() {
        synchronized (occupierLock) {
            Vector occupiedVector = occupiers.keySet().iterator().next();

            if (isStraightConnection(occupiedVector)) {
                SwitchUtil.setSwitchState(crossover, Switchable.State.CLOSED);
            } else {
                SwitchUtil.setSwitchState(crossover, Switchable.State.THROWN);
            }
        }
    }

    @Override
    public int getSectionLength(Vector vector) {
        if (isStraightConnection(vector)) {
            return length;
        } else {
            return crossLength;
        }
    }

    @Override
    public Optional<Range<Integer>> getOccupiedRange(Vector vector, Trainset trainset) {
        synchronized (occupierLock) {
            return Optional.ofNullable(occupiers.get(vector))
                    .map(AbstractMap.SimpleImmutableEntry::getValue);
        }
    }

    @Override
    public void setOccupiedRange(Vector vector, Trainset trainset, Range<Integer> newOwnedRange) {
        synchronized (occupierLock) {
            occupiers.put(vector, new AbstractMap.SimpleImmutableEntry<>(trainset.getAddress(), newOwnedRange));
        }
    }

    @Override
    public void removeOccupier(Vector vector, Trainset trainset) {
        synchronized (occupierLock) {
            occupiers.remove(vector);
        }
    }

    private boolean isStraightConnection(Vector vector) {
        return vector.equals(uplinkStraightConnection.getVector()) ||
                vector.equals(downlinkStraightConnection.getVector());
    }

    @Override
    public void freeAll(Trainset trainset) {
        synchronized (occupierLock) {
            occupiers.entrySet().stream()
                    .filter(entry -> entry.getValue().getKey() == trainset.getAddress())
                    .forEach(entry -> occupiers.remove(entry.getKey()));
        }
    }

    @Override
    public List<Connection> getConnections() {
        List<Connection> connections = new ArrayList<>(List.of(uplinkStraightConnection, downlinkStraightConnection));
        connections.addAll(crossConnections);
        return connections;
    }

    @Override
    public String getOwnerStatus(int ownerId) {
        return null;
    }

    @Override
    public Map<Integer, String> getOwnerSummary() {
        return null;
    }

}
