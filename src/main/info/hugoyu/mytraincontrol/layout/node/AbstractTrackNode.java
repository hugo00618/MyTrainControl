package info.hugoyu.mytraincontrol.layout.node;

import info.hugoyu.mytraincontrol.layout.Allocatable;
import info.hugoyu.mytraincontrol.sensor.SensorChangeListener;
import info.hugoyu.mytraincontrol.sensor.SensorState;
import info.hugoyu.mytraincontrol.trainset.Trainset;
import info.hugoyu.mytraincontrol.util.SensorUtil;
import jmri.Sensor;
import lombok.Getter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class AbstractTrackNode implements Allocatable {

    @Getter
    protected long id;

    @Getter
    protected Set<Long> uplinkNextNodes, downlinkNextNodes;

    protected Map<Long, Integer> costs;

    // map of (sensor, position)
    protected Map<Sensor, Integer> sensors;

    /**
     * @param id
     * @param sensors map of (sensorAddress, location)
     */
    protected AbstractTrackNode(long id, Map<Integer, Integer> sensors) {
        this.id = id;
        uplinkNextNodes = new HashSet<>();
        downlinkNextNodes = new HashSet<>();
        costs = new HashMap<>();

        if (sensors != null) {
            this.sensors = constructSensors(sensors);
        }
    }

    public abstract String getOwnerStatus(int ownerId);

    public abstract Map<Integer, String> getOwnerSummary();

    public abstract int getCostToNode(long toNode, Long previousNode);

    public void addConnection(long nextNode, int cost, boolean isUplink) {
        if (isUplink) {
            uplinkNextNodes.add(nextNode);
        } else {
            downlinkNextNodes.add(nextNode);
        }
        costs.put(nextNode, cost);
    }

    private Map<Sensor, Integer> constructSensors(Map<Integer, Integer> sensors) {
        return sensors.entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> constructSensor(entry.getKey(), entry.getValue()),
                        Map.Entry::getValue));
    }

    private Sensor constructSensor(int address, int position) {
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
                Trainset owner = getOwner(sensors.get(sensor));
                if (owner != null) {
                    owner.calibrate(id, position, sensorState);
                }
            }
        });
    }

    protected abstract Trainset getOwner(int sensorLocation);
}
