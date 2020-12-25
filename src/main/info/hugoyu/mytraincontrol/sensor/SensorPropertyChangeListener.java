package info.hugoyu.mytraincontrol.sensor;

import jmri.Sensor;
import lombok.AllArgsConstructor;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

@AllArgsConstructor
public class SensorPropertyChangeListener implements PropertyChangeListener {

    private SensorChangeListener listener;

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        int newVal = (int) evt.getNewValue();

        if (newVal == Sensor.ACTIVE) {
            listener.onEnter();
        } else if (newVal == Sensor.INACTIVE) {
            listener.onExit();
        }
    }

}
