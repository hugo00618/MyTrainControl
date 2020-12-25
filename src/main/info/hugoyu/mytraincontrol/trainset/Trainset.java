package info.hugoyu.mytraincontrol.trainset;

import info.hugoyu.mytraincontrol.commandstation.AbstractCommandStationTask;
import info.hugoyu.mytraincontrol.commandstation.CommandStation;
import info.hugoyu.mytraincontrol.commandstation.SetLightTask;
import info.hugoyu.mytraincontrol.commandstation.SetSpeedTask;
import info.hugoyu.mytraincontrol.commandstation.TaskExecutionListener;
import info.hugoyu.mytraincontrol.json.TrainsetProfileJsonProvider;
import info.hugoyu.mytraincontrol.sensor.SensorChangeListener;
import info.hugoyu.mytraincontrol.sensor.SensorPropertyChangeListener;
import jmri.InstanceManager;
import jmri.Sensor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j;

import java.io.FileNotFoundException;

@Setter
@Getter
@Log4j
public class Trainset implements TaskExecutionListener {

    private int address;
    private String name;

    private TrainsetProfile profile;

    private double cSpeed; // current speed

    private volatile double tSpeed; // targeting speed
    private final Object tSpeedLock = new Object();

    private volatile double dist; // distance to move
    private final Object distLock = new Object();

    private boolean isLightOn = true;

    public Trainset(int address, String name, String profileFilename) throws FileNotFoundException {
        this.address = address;
        this.name = name;

        profile = TrainsetProfileJsonProvider.parseJSON(profileFilename);

        cSpeed = 0;
        tSpeed = 0;
        dist = 0.0;

        Sensor sensor = InstanceManager.sensorManagerInstance().
                provideSensor("22");

        sensor.addPropertyChangeListener(new SensorPropertyChangeListener(new SensorChangeListener() {
            @Override
            public void onEnter() {
                if (address == 3) {
                    log.debug("Changing distance to 976");
                    synchronized (distLock) {
                        dist = 976;
                    }
                }
            }

            @Override
            public void onExit() {
                if (address == 3) {
                    log.debug("Changing distance to 16");
                    synchronized (distLock) {
                        dist = 16;
                    }
                }
            }
        }));
    }

    public void move(double dist) {
        synchronized (distLock) {
            this.dist = dist;
        }

        sendSetSpeedTask(System.currentTimeMillis());
    }

    private void sendSetSpeedTask(long taskCreationTime) {
        CommandStation.getInstance().addTask(new SetSpeedTask(this, taskCreationTime));
    }

    private void sendSetSpeedTask(long taskCreationTime, long delay) {
        CommandStation.getInstance().addTask(new SetSpeedTask(this, taskCreationTime, delay));
    }

    @Override
    public void prepareForSetSpeedTaskExecution(AbstractCommandStationTask task) {
        synchronized (tSpeedLock) {
            synchronized (distLock) {
                long currentTime = System.currentTimeMillis();
                double deltaT = (currentTime - task.getTaskCreationTime()) / 1000.0;

                dist -= cSpeed * deltaT;
                dist = Math.max(0, dist);
                log.debug(String.format("%s: remaining distance %f", getName(), dist));
                double minimumStoppingDistance = profile.getMinimumStoppingDistance(cSpeed);
                log.debug(String.format("%s: min stop distance %f", getName(), minimumStoppingDistance));

                if (dist > minimumStoppingDistance) {
                    tSpeed = profile.getMaxSpeed();
                } else {
                    tSpeed = 0;
                }
                log.debug(String.format("%s: tSpeed %f", getName(), tSpeed));

                if (cSpeed != tSpeed) {
                    // TODO: deltaT
                    if (!task.isDelayedTask()) {
                        boolean isAcc = cSpeed < tSpeed;

                        double a = isAcc ? profile.getAccRate() : profile.getDecRate();
                        if (dist < minimumStoppingDistance) {
                            a = -cSpeed * cSpeed / 2 / dist;
                        }
                        cSpeed += a * deltaT;

                        if (isAcc) {
                            cSpeed = Math.min(cSpeed, tSpeed);
                        } else {
                            cSpeed = Math.max(cSpeed, tSpeed);
                        }
                    }
                    log.debug(String.format("%s: cSpeed %f", getName(), cSpeed));

                    if (cSpeed != tSpeed) {
                        sendSetSpeedTask(currentTime);
                    } else {
                        double coastingDistance = dist - minimumStoppingDistance;
                        if (coastingDistance > 0) {
                            long coastingTime = (long) (coastingDistance / cSpeed * 1000);
                            log.debug(String.format("%s: coasting for %dms", getName(), coastingTime));
                            sendSetSpeedTask(currentTime, coastingTime);
                        }
                    }

                }
            }
        }
    }

    public float getThrottle() {
        return profile.getThrottle(cSpeed);
    }

    public void setIsLightOn(boolean isLightOn) {
        this.isLightOn = isLightOn;
        CommandStation.getInstance().addTask(new SetLightTask(this));
    }

}
