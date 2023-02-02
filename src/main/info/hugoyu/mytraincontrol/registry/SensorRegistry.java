package info.hugoyu.mytraincontrol.registry;

import info.hugoyu.mytraincontrol.exception.InvalidIdException;
import info.hugoyu.mytraincontrol.layout.Vector;
import jmri.Sensor;

import java.util.HashMap;
import java.util.Map;

public class SensorRegistry {

    private static SensorRegistry instance;

    /**
     * map<sensor, owning track node>
     */
    private Map<Sensor, Vector> sensors;

    private SensorRegistry() {
        sensors = new HashMap<>();
    }

    public static SensorRegistry getInstance() {
        if (instance == null) {
            instance = new SensorRegistry();
        }
        return instance;
    }

    public void registerSensor(Sensor sensor,  Vector nodeVector) {
        if (sensors.containsKey(sensor)) {
            throw new InvalidIdException(sensor.getDisplayName(), InvalidIdException.Type.DUPLICATE);
        }
        sensors.put(sensor, nodeVector);
    }
}
