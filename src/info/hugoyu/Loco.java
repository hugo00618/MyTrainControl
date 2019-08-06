package info.hugoyu;

import jmri.*;

import java.util.PriorityQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Loco implements ThrottleListener {
    private int address;
    private String name;
    private LocoProfile profile;

    private boolean throttleOverride = false;
    private float throttle;

    private double vSpeed = 0, rSpeed = 0; // virtual speed, real speed
    private long vt0, rt0; // virtual last updated time, real last update time
    private double targetSpeed;
    private double moveDist = 0;

    static class SpeedUpdate implements Comparable<SpeedUpdate> {
        long timestamp;
        double speed;

        public SpeedUpdate(long timestamp, double speed) {
            this.timestamp = timestamp;
            this.speed = speed;
        }

        @Override
        public int compareTo(SpeedUpdate o) {
            if (this.timestamp < o.timestamp) {
                return -1;
            } else if (this.timestamp > o.timestamp) {
                return 1;
            } else {
                return 0;
            }
        }
    }

    private PriorityQueue<SpeedUpdate> speedUpdateQueue = new PriorityQueue<>();

    private final Object speedLock = new Object(), speedUpdateQueueLock = new Object();

    private ScheduledExecutorService scheduler;

    public Loco(int address, String name, LocoProfile profile) {
        this.address = address;
        this.name = name;
        this.profile = profile;
        InstanceManager.getNullableDefault(ThrottleManager.class).requestThrottle(address, this);
    }

    public void setThrottle(float throttle) {
        synchronized (speedLock) {
            throttleOverride = true;
            this.throttle = throttle;
        }
    }

//    public void stop() {
//        synchronized (speedLock) {
//            moveDist = 0;
//            setTargetSpeed(0);
//        }
//    }

    public void move(double dist) {
        move(dist, profile.getMaxSpeed());
    }

    public void move(double dist, double targetSpeed) {
        synchronized (speedLock) {
            moveDist = dist;
            setTargetSpeed(targetSpeed);
        }
    }

    private void setTargetSpeed(double targetSpeed) {
        synchronized (speedLock) {
            this.targetSpeed = targetSpeed;
            vt0 = System.currentTimeMillis();
        }
    }

    public void addRealSpeedUpdate(SpeedUpdate speedUpdate) {
        synchronized (speedUpdateQueueLock) {
            speedUpdateQueue.add(speedUpdate);
        }
    }

    private void updateRealSpeed(long rt1, double speed) {
        synchronized (speedLock) {
            long rdt = rt1 - rt0;

            double deltaD = rSpeed * rdt / 1000;
            moveDist -= deltaD;

            this.rSpeed = speed;
            rt0 = rt1;
        }
    }

    /**
     * @return if speed has changed
     */
    private boolean updateSpeed() {
        double oldSpeed = vSpeed;

        if (targetSpeed != vSpeed || moveDist > 0) {
            long vt1 = System.currentTimeMillis();
            long vdt = vt1 - vt0; // virtual delta t

            if (moveDist > 0) {
                // poll speedUpdateQueue and update rSpeed and moveDist
                synchronized (speedUpdateQueueLock) {
                    while (!speedUpdateQueue.isEmpty()) {
                        SpeedUpdate speedUpdate = speedUpdateQueue.poll();
                        updateRealSpeed(speedUpdate.timestamp, speedUpdate.speed);
                    }
                }

                // if no speed adjustments
                if (rSpeed >= targetSpeed) {
                    updateRealSpeed(System.currentTimeMillis(), rSpeed);
                }

                // check if needs to stop
                if (profile.isNeedToStop(rSpeed, moveDist)) {
                    setTargetSpeed(0);
                } else if (targetSpeed < rSpeed) {
                    setTargetSpeed(rSpeed);
                }
            }

            if (targetSpeed != vSpeed) {
                double a = 0;
                a = targetSpeed > vSpeed ? profile.getAccRate() : profile.getDecRate();
                vSpeed += a * vdt / 1000;
                vSpeed = Math.min(Math.max(vSpeed, 0), profile.getMaxSpeed());
//                System.out.println(vSpeed + " " + targetSpeed);
            }

            vt0 = vt1;
        }

        return oldSpeed != vSpeed;
    }

    @Override
    public void notifyStealThrottleRequired(LocoAddress locoAddress) {

    }

    @Override
    public void notifyThrottleFound(DccThrottle dccThrottle) {
        System.out.println(name + ": throttle found");

        // update speed every millisecond
        scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                synchronized (speedLock) {
                    if (throttleOverride) {
//                        ThrottleControlThread.getInstance().
//                                addTask(address, new ThrottleControlThread.ThrottleControlTask(dccThrottle, throttle));
                        throttleOverride = false;
                    } else if (updateSpeed()) {
                        ThrottleControlThread.getInstance().
                                addTask(address, new ThrottleControlThread.ThrottleControlTask(Loco.this, dccThrottle, profile.getThrottle(vSpeed), vSpeed));
                    }
                }
            }
        }, 0, 1, TimeUnit.MICROSECONDS);
    }

    @Override
    public void notifyFailedThrottleRequest(LocoAddress locoAddress, String s) {

    }

    @Override
    public void notifyDecisionRequired(LocoAddress locoAddress, DecisionType decisionType) {

    }

    public void stopControlThread() {
        if (scheduler != null) scheduler.shutdown();
    }

}
