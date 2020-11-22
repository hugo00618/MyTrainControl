package info.hugoyu.mytraincontrol.trainset;

import info.hugoyu.mytraincontrol.commandstation.CommandStation;
import info.hugoyu.mytraincontrol.commandstation.CommandStationTask;
import info.hugoyu.mytraincontrol.commandstation.SetSpeedTask;
import info.hugoyu.mytraincontrol.registries.ThrottleRegistry;
import jmri.DccThrottle;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;

@Setter
@Getter
@Log4j
public class Trainset extends AbstractTrainset {

    private TrainsetProfile profile;

    private double cSpeed; // current speed

    private volatile double tSpeed; // targeting speed
    private final Object tSpeedLock = new Object();

    private volatile double dist; // distance to move
    private final Object distLock = new Object();

    public Trainset(int address, String name, String profileFilename) throws FileNotFoundException {
        super(address, name);

        profile = TrainsetProfileParser.parseJSON(profileFilename);

        cSpeed = 0;
        tSpeed = 0;
        dist = 0.0;
    }

    public void move(int dist) {
        synchronized (distLock) {
            this.dist = dist;
        }

        sendSetSpeedTask();
    }

    private void sendSetSpeedTask() {
        CommandStation.getInstance().addTask(new SetSpeedTask(this));
    }

    private void sendSetSpeedTask(long delay) {
        CommandStation.getInstance().addTask(new SetSpeedTask(this, delay));
    }

    @Override
    public void prepareForSetSpeedTaskExecution(CommandStationTask task) {
        synchronized (tSpeedLock) {
            synchronized (distLock) {
                double deltaT = (System.currentTimeMillis() - task.getTaskCreationTime()) / 1000.0;

                dist -= cSpeed * deltaT;
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
                        cSpeed += a * deltaT;

                        if (isAcc) {
                            cSpeed = Math.min(cSpeed, tSpeed);
                        } else {
                            cSpeed = Math.max(cSpeed, tSpeed);
                        }
                    }
                    log.debug(String.format("%s: cSpeed %f", getName(), cSpeed));

                    if (cSpeed != tSpeed) {
                        sendSetSpeedTask();
                    } else {
                        double coastingDistance = dist - minimumStoppingDistance;
                        if (coastingDistance > 0) {
                            long coastingTime = (long) (coastingDistance / cSpeed * 1000);
                            log.debug(String.format("%s: coasting for %dms", getName(), coastingTime));
                            sendSetSpeedTask(coastingTime);
                        }
                    }

                }
            }
        }
    }

    @Override
    public float getThrottle() {
        return profile.getThrottle(cSpeed);
    }
}
