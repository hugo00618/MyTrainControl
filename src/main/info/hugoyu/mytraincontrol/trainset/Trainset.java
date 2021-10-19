package info.hugoyu.mytraincontrol.trainset;

import info.hugoyu.mytraincontrol.commandstation.CommandStation;
import info.hugoyu.mytraincontrol.commandstation.task.AbstractCommandStationTask;
import info.hugoyu.mytraincontrol.commandstation.task.TaskExecutionListener;
import info.hugoyu.mytraincontrol.commandstation.task.impl.SetDirectionTask;
import info.hugoyu.mytraincontrol.commandstation.task.impl.SetLightTask;
import info.hugoyu.mytraincontrol.commandstation.task.impl.SetSpeedTask;
import info.hugoyu.mytraincontrol.layout.Route;
import info.hugoyu.mytraincontrol.layout.movingblock.MovingBlockManager;
import info.hugoyu.mytraincontrol.sensor.SensorState;
import info.hugoyu.mytraincontrol.util.LayoutUtil;
import info.hugoyu.mytraincontrol.util.TrainUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Setter
@Log4j
public class Trainset implements TaskExecutionListener {
    @Getter
    private final int address;

    @Getter
    private final String name;

    @Getter
    private double cSpeed; // current speed

    private volatile double tSpeed; // targeting speed
    private final Object tSpeedLock = new Object();

    private volatile double distToMove;
    private volatile double movedDist;
    private final Object distLock = new Object();

    private TrainsetProfile profile;

    @Getter
    private boolean isLightOn = true;

    @Getter
    private boolean isForward = true;

    private List<Long> allocatedNodes = new ArrayList<>();
    private final Object allocatedNodesLock = new Object();

    private final MovingBlockManager movingBlockManager = new MovingBlockManager(this);
    private Thread movingBlockManagerThread;

    public Trainset(int address, String name, String profileFilename) {
        this.address = address;
        this.name = name;
        profile = TrainUtil.getTrainsetProfile(profileFilename);
    }

    public void move(Route route) {
        if (movingBlockManagerThread != null && movingBlockManagerThread.isAlive()) {
            throw new RuntimeException("Train is still running");
        }
        setIsForward(route.isUplink());

        movingBlockManager.prepareToMove(route);
        movingBlockManagerThread = new Thread(movingBlockManager.getNewMovingBlockRunnable());
        movingBlockManagerThread.start();
    }

    public double addDistToMove(double addDist) {
        synchronized (distLock) {
            double newDistToMove = this.distToMove + addDist;
            setDistToMove(newDistToMove);
            return newDistToMove;
        }
    }

    public void setDistToMove(double distToMove) {
        log.debug("setDistToMove " + distToMove);
        synchronized (distLock) {
            this.distToMove = distToMove;
        }
        sendSetSpeedTask(System.currentTimeMillis());
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
                movedDist += deltaD;
                distToMove -= deltaD;
                distToMove = Math.max(0, distToMove);

                log.debug(String.format("%s: deltaT %f", name, deltaT));
                log.debug(String.format("%s: deltaD %f", name, deltaD));
                log.debug(String.format("%s: remaining distance %f", name, distToMove));

                // update target speed
                double minimumStoppingDistance = getCurrentMinimumStoppingDistance();
                if (distToMove > minimumStoppingDistance) {
                    tSpeed = profile.getTopSpeed();
                } else {
                    tSpeed = 0;
                }
                log.debug(String.format("%s: tSpeed %f", name, tSpeed));

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
                log.debug(String.format("%s: cSpeed %f", name, cSpeed));
                log.debug(String.format("%s: min stop distance %f", name, minimumStoppingDistance));

                // send next speed task
                if (distToMove > 0) {
                    if (cSpeed == 0 || cSpeed != tSpeed) {
                        sendSetSpeedTask(currentTime);
                    } else {
                        double coastingDistance = distToMove - minimumStoppingDistance;
                        if (coastingDistance > 0) {
                            long coastingTime = (long) (coastingDistance / cSpeed * 1000);
                            log.debug(String.format("%s: coasting for %dms", name, coastingTime));
                            sendSetSpeedTask(currentTime, coastingTime);
                        }
                    }
                }

                distLock.notifyAll();
            }
        }
    }

    public void calibrate(long nodeId, int sensorPosition, SensorState sensorState) {
        if (movingBlockManagerThread != null && movingBlockManagerThread.isAlive()) {
            movingBlockManager.calibrate(nodeId, sensorPosition, sensorState);
        }
    }

    public double getCurrentMinimumStoppingDistance() {
        return profile.getMinimumStoppingDistance(cSpeed);
    }

    public double resetMovedDist() {
        synchronized (distLock) {
            double res = movedDist;
            movedDist = 0;
            return res;
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

    public int getTotalLength() {
        return profile.getTotalLength();
    }

    public float getThrottle() {
        return profile.getThrottle(cSpeed);
    }

    public void setIsLightOn(boolean isLightOn) {
        this.isLightOn = isLightOn;
        CommandStation.getInstance().addTask(new SetLightTask(this));
    }

    public void setIsForward(boolean isForward) {
        this.isForward = isForward;
        CommandStation.getInstance().addTask(new SetLightTask(this));
        CommandStation.getInstance().addTask(new SetDirectionTask(this));
    }

    public void addAllocatedNode(long nodeId) {
        synchronized (allocatedNodesLock) {
            if (!allocatedNodes.contains(nodeId)) {
                allocatedNodes.add(nodeId);
            }
        }
    }

    public void removeAllocatedNode(Long nodeId) {
        synchronized (allocatedNodesLock) {
            allocatedNodes.remove(nodeId);
        }
    }

    public Long getFirstAllocatedNode() {
        synchronized (allocatedNodesLock) {
            return getAllocatedNode(0);
        }
    }

    public Long getLastAllocatedNode() {
        synchronized (allocatedNodesLock) {
            return getAllocatedNode(allocatedNodes.size() - 1);
        }
    }

    private Long getAllocatedNode(int idx) {
        synchronized (allocatedNodesLock) {
            try {
                return allocatedNodes.get(idx);
            } catch (IndexOutOfBoundsException e) {
                return null;
            }
        }
    }

    public Map<Long, String> getAllocatedNodes() {
        synchronized (allocatedNodesLock) {
            return allocatedNodes.stream()
                    .collect(Collectors.toMap(
                            nodeId -> nodeId,
                            nodeId -> LayoutUtil.getNode(nodeId).getOwnerStatus(address)));
        }
    }

}
