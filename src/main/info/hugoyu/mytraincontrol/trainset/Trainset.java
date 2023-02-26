package info.hugoyu.mytraincontrol.trainset;

import com.google.common.collect.Range;
import info.hugoyu.mytraincontrol.ato.AutomaticTrainOperationThread;
import info.hugoyu.mytraincontrol.commandstation.task.AbstractCommandStationTask;
import info.hugoyu.mytraincontrol.commandstation.task.TaskExecutionListener;
import info.hugoyu.mytraincontrol.commandstation.task.impl.SetDirectionTask;
import info.hugoyu.mytraincontrol.commandstation.task.impl.SetLightTask;
import info.hugoyu.mytraincontrol.commandstation.task.impl.SetSpeedTask;
import info.hugoyu.mytraincontrol.exception.InvalidIdException;
import info.hugoyu.mytraincontrol.exception.NodeAllocationException;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Setter
@Log4j
public class Trainset implements TaskExecutionListener {
    @Getter
    private final int address;

    @Getter
    private final String name;

    @Getter
    private double cSpeed; // current speed

    @Getter
    private double tSpeed; // targeting speed
    private final Object tSpeedLock = new Object();

    private Distance distance = new Distance();

    private final TrainsetProfile profile;

    @Getter
    private LightState lightState = LightState.UNKNOWN;

    // forward relative to track's default direction
    private boolean isForward = true;

    // whether motor is placed reversed on the track
    private final boolean isMotorReversed;

    private List<Long> allocatedNodes = new ArrayList<>();
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
        setAllocatedNodes(getAllocatedStationTrack().get().getNodeIds(route.isUplink()));

        movingBlockManager.prepareToMove(route);
        movingBlockManagerThread = new Thread(movingBlockManager.getNewMovingBlockRunnable());
        movingBlockManagerThread.start();
    }

    @Override
    public void prepareForSetSpeedTaskExecution(AbstractCommandStationTask task) {
        synchronized (tSpeedLock) {
            final long currentTime = System.currentTimeMillis();

            // update distance
            distance.update();

            // update target speed
            double minimumStoppingDistance = getCurrentMinimumStoppingDistance();
            if (distance.getDistToMove() > minimumStoppingDistance) {
                tSpeed = profile.getTopSpeed();
            } else {
                tSpeed = 0;
            }
            log.debug(String.format("%s: tSpeed %f", name, tSpeed));

            // update cSpeed if train was not coasting
            if (!task.isDelayedTask()) {
                boolean isAcc = cSpeed < tSpeed;

                double a = isAcc ? profile.getAccRate() : profile.getDecRate();
                final double distToMove = distance.getDistToMove();
                if (distToMove < minimumStoppingDistance) {
                    a = -cSpeed * cSpeed / 2 / distToMove;
                }
                double deltaT = (currentTime - task.getTaskCreationTime()) / 1000.0;
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
            final double distToMove = distance.getDistToMove();
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
        }
    }

    private void sendSetSpeedTask(long taskCreationTime) {
        CommandStationUtil.addTask(new SetSpeedTask(this, taskCreationTime));
    }

    private void sendSetSpeedTask(long taskCreationTime, long delay) {
        CommandStationUtil.addTask(new SetSpeedTask(this, taskCreationTime, delay));
    }

    public void calibrate(Vector nodeVector, int sensorOffset, SensorState sensorState) {
        if (movingBlockManagerThread != null && movingBlockManagerThread.isAlive()) {
            movingBlockManager.calibrate(nodeVector, sensorOffset, sensorState);
        }
    }

    public double getCurrentMinimumStoppingDistance() {
        return profile.getMinimumStoppingDistance(cSpeed);
    }

    public void addDistToMove(double dist) {
        distance.addDistToMove(dist);
    }

    public void setDistToMove(double distToMove) {
        distance.setDistToMove(distToMove);
        setIsLightOn(LightState.ON);
    }

    public double getDistToMove() {
        return distance.getDistToMove();
    }

    public double resetMovedDist() {
        return distance.resetMovedDist();
    }

    public void waitDistUpdate() {
        distance.waitDistUpdate();
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
        return !isMotorReversed ? isForward : !isForward;
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

    public void resetAllocatedNodes() {
        synchronized (allocatedNodesLock) {
            while (allocatedNodes.size() > 2) {
                LayoutUtil.getNode(allocatedNodes.get(0), allocatedNodes.get(1)).freeAll(this);
                allocatedNodes.remove(0);
            }

            try {
                LayoutUtil.getStationTrackNode(allocatedNodes.get(0), allocatedNodes.get(1))
                                .occupyStationTrack(this);
            } catch (NodeAllocationException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public Vector getFirstAllocatedVector() {
        synchronized (allocatedNodesLock) {
            return new Vector(allocatedNodes.get(0), allocatedNodes.get(1));
        }
    }

    /**
     * @return allocated station track node if trainset occupies only one node and the node is a station track node
     */
    public Optional<StationTrackNode> getAllocatedStationTrack() {
        synchronized (allocatedNodesLock) {
            if (allocatedNodes.size() != 2) return Optional.empty();

            try {
                return Optional.of(LayoutUtil.getStationTrackNode(getFirstAllocatedVector()));
            } catch (InvalidIdException e) {
                return Optional.empty();
            }
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

    public Map<Vector, Range<Integer>> getAllocatedNodesSummary() {
        Map<Vector, Range<Integer>> result = new HashMap<>();
        for (int i = 0; i < allocatedNodes.size() - 1; i++) {
            Vector nodeVector = new Vector(allocatedNodes.get(i), allocatedNodes.get(i + 1));
            result.put(nodeVector,
                    LayoutUtil.getNode(nodeVector).getOccupiedRangeImmediately(nodeVector, this)
                            .orElse(null));
        }
        return result;
    }

    public void activateAto() {
        if (atoThread != null && atoThread.isAlive()) {
            throw new RuntimeException(String.format("%s: ATO is already running", getName()));
        }

        atoThread = new AutomaticTrainOperationThread(this);
        atoThread.start();
    }

    public void deactivateAto() {
        if (atoThread == null || atoThread.isAlive()) {
            throw new RuntimeException(String.format("%s: ATO is not running", getName()));
        }

        atoThread.signalTerminate();
    }

    private class Distance {
        private double distToMove;
        private double movedDist;
        private long lastUpdated;
        private final Object distLock = new Object();

        public void addDistToMove(double dist) {
            synchronized (distLock) {
                setDistToMove(this.distToMove + dist);
            }
        }

        public void setDistToMove(double distToMove) {
            synchronized (distLock) {
                this.distToMove = distToMove;
                sendSetSpeedTask(System.currentTimeMillis());
            }
        }

        public double getDistToMove() {
            synchronized (distLock) {
                update();
                return distToMove;
            }
        }

        public double resetMovedDist() {
            synchronized (distLock) {
                update();
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
                    throw new RuntimeException(e);
                }
            }
        }

        private void update() {
            synchronized (distLock) {
                long currentTime = System.currentTimeMillis();
                double deltaT = (currentTime - lastUpdated) / 1000.0;
                double deltaD = cSpeed * deltaT;
                lastUpdated = currentTime;

                distToMove = Math.max(0, distToMove - deltaD);
                movedDist += deltaD;

                distLock.notifyAll();
            }
        }
    }

}
