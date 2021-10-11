package info.hugoyu.mytraincontrol.layout.node.impl;

import com.google.common.collect.Range;
import info.hugoyu.mytraincontrol.exception.NodeAllocationException;
import info.hugoyu.mytraincontrol.layout.BlockSectionResult;
import info.hugoyu.mytraincontrol.layout.node.AbstractTrackNode;
import info.hugoyu.mytraincontrol.sensor.SensorChangeListener;
import info.hugoyu.mytraincontrol.sensor.SensorState;
import info.hugoyu.mytraincontrol.trainset.Trainset;
import info.hugoyu.mytraincontrol.util.SensorUtil;
import info.hugoyu.mytraincontrol.util.TrainUtil;
import jmri.Sensor;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class RegularTrackNode extends AbstractTrackNode implements Comparable<RegularTrackNode> {

    protected int length;

    // map of (trainsetAddress, ownedRange)
    protected Map<Integer, Range<Integer>> owners = new HashMap<>();
    private final Object ownersLock = new Object();

    // map of (sensorAddress, position)
    private Map<Sensor, Integer> sensors = new HashMap<>();

    /**
     * @param id0     id of the current section
     * @param id1     id of the next section (if any)
     * @param length  length of the current section
     * @param sensors map of (sensorAddress, location)
     */
    public RegularTrackNode(long id0, Long id1, int length, Map<Integer, Integer> sensors) {
        super(id0);

        this.length = length;

        if (id1 != null) {
            addConnection(id1, length);
        }

        if (sensors != null) {
            this.sensors = sensors.entrySet().stream()
                    .collect(Collectors.toMap(
                            entry -> constructSensor(entry.getKey(), entry.getValue()),
                            Map.Entry::getValue));
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
                    .allMatch(entry -> !entry.getValue().isConnected(allocatingRange) ||
                            entry.getValue().intersection(allocatingRange).isEmpty());
        }
    }

    private Sensor constructSensor(int address, int position) {
        AbstractTrackNode nodeInstance = this;
        return SensorUtil.getSensor(address, new SensorChangeListener() {
            @Override
            public void onEnter(Sensor sensor) {
                calibrateOwnerMovingBlockManager(sensor, SensorState.ENTER);
            }

            @Override
            public void onExit(Sensor sensor) {
                calibrateOwnerMovingBlockManager(sensor, SensorState.EXIT);
            }

            private void calibrateOwnerMovingBlockManager(Sensor sensor, SensorState sensorState) {
                Trainset owner = getOwner(sensor);
                if (owner != null) {
                    owner.calibrate(nodeInstance.getId(), position, sensorState);
                }
            }
        });
    }

    private Trainset getOwner(Sensor sensor) {
        int sensorLocation = sensors.get(sensor);
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
        if (!owners.containsKey(ownerId)) {
            return null;
        }
        return owners.get(ownerId).toString();
    }
}
