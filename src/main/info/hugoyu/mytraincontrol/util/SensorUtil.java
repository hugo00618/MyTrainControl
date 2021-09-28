package info.hugoyu.mytraincontrol.util;

import info.hugoyu.mytraincontrol.sensor.SensorChangeListener;
import info.hugoyu.mytraincontrol.sensor.SensorPropertyChangeListener;
import jmri.InstanceManager;
import jmri.Sensor;

public class SensorUtil {

    public static Sensor getSensor(int address, SensorChangeListener listener) {
        Sensor sensor = InstanceManager.sensorManagerInstance().
                provideSensor(String.valueOf(address));
        sensor.addPropertyChangeListener(new SensorPropertyChangeListener(sensor, listener));
        return sensor;
    }
}
