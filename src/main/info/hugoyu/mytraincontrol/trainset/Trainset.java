package info.hugoyu.mytraincontrol.trainset;

import info.hugoyu.mytraincontrol.commandstation.task.AbstractCommandStationTask;
import info.hugoyu.mytraincontrol.commandstation.CommandStation;
import info.hugoyu.mytraincontrol.commandstation.task.impl.SetLightTask;
import info.hugoyu.mytraincontrol.commandstation.task.impl.SetSpeedTask;
import info.hugoyu.mytraincontrol.commandstation.task.TaskExecutionListener;
import info.hugoyu.mytraincontrol.json.TrainsetProfileJsonProvider;
import info.hugoyu.mytraincontrol.layout.Route;
import info.hugoyu.mytraincontrol.layout.MovingBlockManagerRunnable;
import info.hugoyu.mytraincontrol.sensor.SensorChangeListener;
import info.hugoyu.mytraincontrol.sensor.SensorPropertyChangeListener;
import jmri.InstanceManager;
import jmri.Sensor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j;

import java.io.FileNotFoundException;
import java.util.LinkedHashSet;

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

    private volatile double distToMove;
    public final Object distLock = new Object();

    private boolean isLightOn = true;

    private LinkedHashSet<String> allocatedNodes = new LinkedHashSet<>();

    public Trainset(int address, String name, String profileFilename) throws FileNotFoundException {
        this.address = address;
        this.name = name;

        profile = TrainsetProfileJsonProvider.parseJSON(profileFilename);

        Sensor sensor = InstanceManager.sensorManagerInstance().
                provideSensor("22");

        sensor.addPropertyChangeListener(new SensorPropertyChangeListener(new SensorChangeListener() {
            @Override
            public void onEnter() {
                if (address == 3) {
                    log.debug("Calibrating distance to 976");
                    updateDistToMove(976);
                }
            }

            @Override
            public void onExit() {
                if (address == 3) {
                    log.debug("Calibrating distance to 16");
                    updateDistToMove(16);
                }
            }
        }));
    }

    public void move(int distToMove) {
        setDistToMove(distToMove);
    }

    public void move(Route route) {
        new Thread(new MovingBlockManagerRunnable(this, route)).start();
    }

    public void setDistToMove(double distToMove) {
        updateDistToMove(distToMove);
        sendSetSpeedTask(System.currentTimeMillis());
    }

    public void updateDistToMove(double distToMove) {
        synchronized (distLock) {
            this.distToMove = distToMove;
        }
    }

    public void sendSetSpeedTask(long taskCreationTime) {
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
                double deltaD = cSpeed * deltaT;

                // update distance
                distToMove -= deltaD;
                distToMove = Math.max(0, distToMove);
                double minimumStoppingDistance = getCurrentMinimumStoppingDistance();

                log.debug(String.format("%s: deltaT %f", getName(), deltaT));
                log.debug(String.format("%s: deltaD %f", getName(), deltaD));
                log.debug(String.format("%s: remaining distance %f", getName(), distToMove));
                log.debug(String.format("%s: min stop distance %f", getName(), minimumStoppingDistance));

                // update target speed
                if (distToMove > minimumStoppingDistance) {
                    tSpeed = profile.getMaxSpeed();
                } else {
                    tSpeed = 0;
                }
                log.debug(String.format("%s: tSpeed %f", getName(), tSpeed));

                // update cSpeed if train was not coasting
                if (!task.isDelayedTask()) {
                    boolean isAcc = cSpeed < tSpeed;

                    double a = isAcc ? profile.getAccRate() : profile.getDecRate();
                    if (distToMove < minimumStoppingDistance) {
                        a = -cSpeed * cSpeed / 2 / distToMove;
                    }
                    cSpeed += a * deltaT;

                    if (isAcc) {
                        cSpeed = Math.min(cSpeed, tSpeed);
                    } else {
                        cSpeed = Math.max(cSpeed, tSpeed);
                    }
                }
                log.debug(String.format("%s: cSpeed %f", getName(), cSpeed));

                // send next speed task
                if (cSpeed != tSpeed) {
                    sendSetSpeedTask(currentTime);
                } else {
                    double coastingDistance = distToMove - minimumStoppingDistance;
                    if (coastingDistance > 0) {
                        long coastingTime = (long) (coastingDistance / cSpeed * 1000);
                        log.debug(String.format("%s: coasting for %dms", getName(), coastingTime));
                        sendSetSpeedTask(currentTime, coastingTime);
                    }
                }

                distLock.notifyAll();
            }
        }
    }

    public double getCurrentMinimumStoppingDistance() {
        return profile.getMinimumStoppingDistance(cSpeed);
    }

    public double getDistToMove() {
        synchronized (distLock) {
            return distToMove;
        }
    }

    public void waitDistUpdate() {
        synchronized (distLock) {
            try {
                distLock.wait();
            } catch (InterruptedException e) {

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
