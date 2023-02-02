package info.hugoyu.mytraincontrol.trainset;

import info.hugoyu.mytraincontrol.ato.AutomaticTrainOperationThread;
import info.hugoyu.mytraincontrol.commandstation.task.AbstractCommandStationTask;
import info.hugoyu.mytraincontrol.commandstation.task.TaskExecutionListener;
import info.hugoyu.mytraincontrol.commandstation.task.impl.SetDirectionTask;
import info.hugoyu.mytraincontrol.commandstation.task.impl.SetLightTask;
import info.hugoyu.mytraincontrol.commandstation.task.impl.SetSpeedTask;
import info.hugoyu.mytraincontrol.layout.Position;
import info.hugoyu.mytraincontrol.layout.Route;
import info.hugoyu.mytraincontrol.layout.Vector;
import info.hugoyu.mytraincontrol.layout.movingblock.MovingBlockManager;
import info.hugoyu.mytraincontrol.layout.node.impl.StationTrackNode;
import info.hugoyu.mytraincontrol.sensor.SensorState;
import info.hugoyu.mytraincontrol.util.AllocateUtil;
import info.hugoyu.mytraincontrol.util.CommandStationUtil;
import info.hugoyu.mytraincontrol.util.LayoutUtil;
import info.hugoyu.mytraincontrol.util.LightState;
import info.hugoyu.mytraincontrol.util.TrainUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
    private LightState lightState = LightState.UNKNOWN;

    // forward relative to track's default direction
    private boolean isForward = true;

    // whether motor is placed reversed on the track
    private boolean isMotorReversed;

    private volatile List<Long> allocatedNodes = new ArrayList<>();
    private final Object allocatedNodesLock = new Object();

    private MovingBlockManager movingBlockManager = new MovingBlockManager(this);
    private Thread movingBlockManagerThread;

    private AutomaticTrainOperationThread atoThread;

    public Trainset(int address, String name, String profileFilename, boolean isMotorReversed) {
        this.address = address;
        this.name = name;
        this.isMotorReversed = isMotorReversed;
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

    public void addDistToMove(double addDist) {
        synchronized (distLock) {
            setDistToMove(this.distToMove + addDist);
        }
    }

    public double getDistToMove() {
        synchronized (distLock) {
            return distToMove;
        }
    }

    public void setDistToMove(double distToMove) {
        log.debug("setDistToMove " + distToMove);
        synchronized (distLock) {
            this.distToMove = distToMove;
        }
        setIsLightOn(LightState.ON);
        sendSetSpeedTask(System.currentTimeMillis());
    }

    public void sendSetSpeedTask(long taskCreationTime) {
        CommandStationUtil.addTask(new SetSpeedTask(this, taskCreationTime));
    }

    private void sendSetSpeedTask(long taskCreationTime, long delay) {
        CommandStationUtil.addTask(new SetSpeedTask(this, taskCreationTime, delay));
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

    public void calibrate(Position sensorPosition, SensorState sensorState) {
        if (movingBlockManagerThread != null && movingBlockManagerThread.isAlive()) {
            movingBlockManager.calibrate(sensorPosition, sensorState);
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

    public double getMovedDist() {
        synchronized (distLock) {
            return movedDist;
        }
    }

    public void waitDistUpdate() {
        synchronized (distLock) {
            try {
                distLock.wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public int getTotalLength() {
        return profile.getTotalLength();
    }

    public float getThrottle() {
        return profile.getThrottle(cSpeed);
    }

    public void setIsLightOn(LightState newLightState) {
        boolean isLightStateChanged = lightState != newLightState;
        if (isLightStateChanged) {
            lightState = newLightState;
            CommandStationUtil.addTask(new SetLightTask(this));
        }
    }

    public void setIsForward(boolean isForward) {
        this.isForward = isForward;
        CommandStationUtil.addTask(new SetDirectionTask(this));
    }

    public boolean isForward() {
        if (isMotorReversed) {
            return !isForward;
        } else {
            return isForward;
        }
    }

    public void addAllocatedNodes(List<Long> nodes) {
        synchronized (allocatedNodesLock) {
            nodes.forEach(node -> {
                if (!allocatedNodes.contains(node)) {
                    allocatedNodes.add(node);
                }
            });
        }
    }

    public void removeFirstAllocatedNode() {
        synchronized (allocatedNodesLock) {
            allocatedNodes.remove(0);
        }
    }

    public Vector getFirstAllocatedVector() {
        synchronized (allocatedNodesLock) {
            return new Vector(allocatedNodes.get(0), allocatedNodes.get(1));
        }
    }

    public StationTrackNode getAllocatedStationTrack() {
        return LayoutUtil.getStationTrackNode(getAllocatedVector());
    }

    private Vector getAllocatedVector() {
        synchronized (allocatedNodesLock) {
            return new Vector(allocatedNodes.get(0), allocatedNodes.get(1));
        }
    }

    public void freeAllNodes() {
        if (movingBlockManagerThread != null && movingBlockManagerThread.isAlive()) {
            movingBlockManagerThread.interrupt();
        }
        movingBlockManager = new MovingBlockManager(this);

        synchronized (allocatedNodesLock) {
            for (int i = 0; i < allocatedNodes.size() - 1; i++) {
                Vector vector = new Vector(allocatedNodes.get(i), allocatedNodes.get(i + 1));
                AllocateUtil.freeAllNodes(vector, this);
            }

            allocatedNodes = new ArrayList<>();
        }
    }

    public void waitForCurrentMoveToFinish() throws InterruptedException {
        if (movingBlockManagerThread != null && movingBlockManagerThread.isAlive()) {
            movingBlockManagerThread.join();
        }
    }

    public Map<Long, String> getAllocatedNodesSummary() {
        synchronized (allocatedNodesLock) {
            return null;
        }
    }

    public interface AtoHandler {
        void onSuccess();
    }

    public void activateAto(AtoHandler callback) {
        if (atoThread == null || !atoThread.isAlive()) {
            atoThread = new AutomaticTrainOperationThread(this);
            atoThread.start();
            callback.onSuccess();
        }
    }

    public void deactivateAto(AtoHandler callback) {
        if (atoThread != null && atoThread.isAlive()) {
            atoThread.signalTerminate();
            callback.onSuccess();
        }
    }

}
