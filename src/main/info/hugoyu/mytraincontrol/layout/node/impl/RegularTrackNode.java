package info.hugoyu.mytraincontrol.layout.node.impl;

import com.google.common.collect.Range;
import info.hugoyu.mytraincontrol.json.layout.RegularTrackJson;
import info.hugoyu.mytraincontrol.layout.Connection;
import info.hugoyu.mytraincontrol.layout.Vector;
import info.hugoyu.mytraincontrol.layout.node.AbstractTrackNode;
import info.hugoyu.mytraincontrol.layout.node.SensorAttachable;
import info.hugoyu.mytraincontrol.registry.TrainsetRegistry;
import info.hugoyu.mytraincontrol.trainset.Trainset;
import info.hugoyu.mytraincontrol.util.GeneralUtil;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

@Log4j2
public class RegularTrackNode extends AbstractTrackNode implements Comparable<RegularTrackNode>, SensorAttachable {

    @Getter
    protected final long id0, id1;

    protected final int length;

    @Getter
    protected final boolean isUplink;

    // direction -> (train address -> occupiedRange))
    protected Vector occupiedVector;
    protected Map<Integer, Range<Integer>> occupiers = new HashMap<>();

    /**
     * @param id0    id of the current section
     * @param id1    id of the next section (if any)
     * @param length length of the current section
     */
    public RegularTrackNode(long id0, long id1, int length,
                            boolean isUplink, boolean isBidirectional) {
        super(isBidirectional);

        this.id0 = id0;
        this.id1 = id1;
        this.length = length;
        this.isUplink = isUplink;
    }

    public RegularTrackNode(RegularTrackJson regularTrackJson, boolean isUplink) {
        this(regularTrackJson.getId0(),
                regularTrackJson.getId1(),
                regularTrackJson.getLength(),
                isUplink,
                regularTrackJson.isBidirectional());
    }

    @Override
    public List<Connection> getConnections() {
        return List.of(new Connection(id0, id1, length, isUplink, isBidirectional));
    }

    @Override
    public int compareTo(RegularTrackNode o) {
        return length - o.length;
    }

    @Override
    public boolean isFree(Trainset trainset, Vector vector, Range<Integer> range) {
        occupierLock.lock();
        try {
            if (occupiedVector == null) {
                return true;
            }
            if (occupiedVector.equals(vector)) {
                return occupiers.values().stream()
                        .noneMatch(occupiedRange -> GeneralUtil.isOverlapping(occupiedRange, range));
            }
            return false;
        } finally {
            occupierLock.unlock();
        }
    }

    @Override
    public void setOccupier(Trainset trainset, Vector vector, Range<Integer> range) {
        occupierLock.lock();
        try {
            occupiedVector = vector;
            occupiers.put(trainset.getAddress(), range);
        } finally {
            occupierLock.unlock();
        }
    }

    @Override
    public Future<Void> updateHardware() {
        // no hardware change required
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public int getSectionLength(Vector vector) {
        return length;
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
        return Optional.ofNullable(occupiers.get(trainset.getAddress()));
    }

    @Override
    public void removeOccupier(Vector vector, Trainset trainset) {
        this.removeOccupier(trainset);
    }

    @Override
    public void freeAll(Trainset trainset) {
        this.removeOccupier(trainset);
    }

    private void removeOccupier(Trainset trainset) {
        occupierLock.lock();
        try {
            occupiers.remove(trainset.getAddress());
            if (occupiers.isEmpty()) {
                occupiedVector = null;
            }
        } finally {
            occupierLock.unlock();
        }
    }

    @Override
    public Trainset getOccupier(int position) {
        occupierLock.lock();
        try {
            if (occupiedVector == null) {
                return null;
            }

            return occupiers.entrySet().stream()
                    .filter(entry -> entry.getValue().contains(position))
                    .findFirst()
                    .map(entry -> TrainsetRegistry.getInstance().getTrainset(entry.getKey()))
                    .orElse(null);
        } finally {
            occupierLock.unlock();
        }
    }

    /**
     * @param isUplink
     * @return nodeId used for finding routes
     */
    public long getNodeIdForRoute(boolean isUplink) {
        return this.isUplink == isUplink ? id1 : id0;
    }

}
