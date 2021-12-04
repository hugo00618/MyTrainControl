package info.hugoyu.mytraincontrol.sensor;

import jmri.Sensor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

@RequiredArgsConstructor
public class SensorPropertyChangeListener implements PropertyChangeListener {

    private static long FLICKER_THRESHOLD_MILLIS = 1000;

    @NonNull
    private Sensor sensor;

    @NonNull
    private SensorChangeListener listener;

    private long lastUpdatedTime = 0;

    private boolean hasFlickered = false;

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        boolean isFlicker = false;
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastUpdatedTime < FLICKER_THRESHOLD_MILLIS) {
            isFlicker = true;
            hasFlickered = true;
        }
        lastUpdatedTime = currentTime;

        if (!isFlicker) {
            updateListener((int) evt.getNewValue());
        }
    }

    private void updateListener(int newVal) {
        if (newVal == Sensor.ACTIVE) {
            listener.onEnter(sensor);
        } else if (newVal == Sensor.INACTIVE) {
            // do not react to LOW events on flickering sensors
            if (!hasFlickered) {
                listener.onExit(sensor);
            }
        }
    }

}
