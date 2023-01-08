package info.hugoyu.mytraincontrol.layout.node.impl;

import com.google.common.collect.Range;
import info.hugoyu.mytraincontrol.exception.NodeAllocationException;
import info.hugoyu.mytraincontrol.json.layout.RegularTrackJson;
import info.hugoyu.mytraincontrol.layout.BlockSectionResult;
import info.hugoyu.mytraincontrol.layout.Position;
import info.hugoyu.mytraincontrol.layout.node.AbstractTrackNode;
import info.hugoyu.mytraincontrol.layout.node.Connection;
import info.hugoyu.mytraincontrol.layout.node.SensorAttachable;
import info.hugoyu.mytraincontrol.trainset.Trainset;
import info.hugoyu.mytraincontrol.util.TrainUtil;
import lombok.Getter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RegularTrackNode extends AbstractTrackNode implements Comparable<RegularTrackNode>, SensorAttachable {

    protected final long id0, id1;

    protected final int length;

    @Getter
    protected final boolean isUplink, isBidirectional;

    // map of (trainsetAddress, ownedRange)
    // ownedRange is with respect to id0
    protected Map<Integer, Range<Integer>> owners = new HashMap<>();
    protected final Object ownersLock = new Object();

    /**
     * @param id0     id of the current section
     * @param id1     id of the next section (if any)
     * @param length  length of the current section
     */
    public RegularTrackNode(long id0, long id1, int length,
                            boolean isUplink, boolean isBidirectional) {
        this.id0 = id0;
        this.id1 = id1;
        this.length = length;
        this.isUplink = isUplink;
        this.isBidirectional = isBidirectional;
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
    public List<Long> getIds() {
        return List.of(id0);
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
        synchronized (ownersLock) {
            int trainsetAddress = trainset.getAddress();
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

    @Override
    public void freeAll(Trainset trainset) throws NodeAllocationException {
        synchronized (ownersLock) {
            int trainsetAddress = trainset.getAddress();
            Range<Integer> ownedRange = owners.get(trainsetAddress);
            if (ownedRange == null) {
                throw new NodeAllocationException(NodeAllocationException.ExceptionType.FREEING_UNOWNED_SECTION,
                        trainset, this, 0);
            }

            owners.remove(trainsetAddress);
            ownersLock.notifyAll();
        }
    }

    protected boolean isFree(Trainset trainset, Range<Integer> allocatingRange) {
        int trainsetAddress = trainset.getAddress();

        synchronized (ownersLock) {
            return owners.entrySet().stream()
                    .filter(entry -> entry.getKey() != trainsetAddress)
                    .allMatch(entry -> !entry.getValue().isConnected(allocatingRange) ||
                            entry.getValue().intersection(allocatingRange).isEmpty());
        }
    }

    @Override
    public Trainset getOccupier(Position position) {
        synchronized (ownersLock) {
            int positionWrtId0 = position.getReferenceNode() == id0 ?
                    position.getOffset() : length - position.getOffset();
            return owners.entrySet().stream()
                    .filter(entry -> entry.getValue().contains(positionWrtId0))
                    .map(entry -> TrainUtil.getTrainset(entry.getKey()))
                    .findFirst()
                    .orElse(null);
        }
    }

    @Override
    public String getOwnerStatus(int ownerId) {
        synchronized (ownersLock) {
            if (!owners.containsKey(ownerId)) {
                return null;
            }
            return owners.get(ownerId).toString();
        }
    }

    @Override
    public Map<Integer, String> getOwnerSummary() {
        return owners.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().toString()));
    }
}
