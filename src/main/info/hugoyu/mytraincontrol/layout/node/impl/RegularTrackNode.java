package info.hugoyu.mytraincontrol.layout.node.impl;

import com.google.common.collect.Range;
import info.hugoyu.mytraincontrol.exception.NodeAllocationException;
import info.hugoyu.mytraincontrol.json.layout.RegularTrackJson;
import info.hugoyu.mytraincontrol.layout.BlockSectionResult;
import info.hugoyu.mytraincontrol.layout.node.AbstractTrackNode;
import info.hugoyu.mytraincontrol.trainset.Trainset;
import info.hugoyu.mytraincontrol.util.TrainUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class RegularTrackNode extends AbstractTrackNode implements Comparable<RegularTrackNode> {

    protected int length;

    // map of (trainsetAddress, ownedRange)
    protected Map<Integer, Range<Integer>> owners = new HashMap<>();
    protected final Object ownersLock = new Object();

    /**
     * @param id0     id of the current section
     * @param id1     id of the next section (if any)
     * @param length  length of the current section
     * @param sensors map of (sensorAddress, location)
     */
    public RegularTrackNode(long id0, Long id1, int length, boolean isUplink, Map<Integer, Integer> sensors) {
        super(id0, sensors);

        this.length = length;

        if (id1 != null) {
            addConnection(id1, length, isUplink);
        }
    }

    public RegularTrackNode(RegularTrackJson regularTrackJson, boolean isUplink) {
        this(regularTrackJson.getId0(),
                regularTrackJson.getId1(),
                regularTrackJson.getLength(),
                isUplink,
                regularTrackJson.getSensors());
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
    protected Trainset getOwner(int sensorLocation) {
        synchronized (ownersLock) {
            return owners.entrySet().stream()
                    .filter(entry -> entry.getValue().contains(sensorLocation))
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

    @Override
    public int getCostToNode(long toNode, Long previousNode) {
        return costs.get(toNode);
    }
}
