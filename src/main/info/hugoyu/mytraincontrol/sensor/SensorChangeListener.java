package info.hugoyu.mytraincontrol.sensor;

import jmri.Sensor;

public interface SensorChangeListener {
    void onEnter(Sensor sensor);
    void onExit(Sensor sensor);
}
