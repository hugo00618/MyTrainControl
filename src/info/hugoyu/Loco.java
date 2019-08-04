package info.hugoyu;

import jmri.*;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Loco implements ThrottleListener {
    private int address;
    private String name;
    private LocoProfile profile;

    private boolean throttleOverride = false;
    private float throttle;
    private double speed = 0;
    private long t0;
    private double targetSpeed;
    private double moveDist = 0;

    private Object speedLock = new Object();

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
            t0 = System.currentTimeMillis();
        }
    }

    /**
     * @return if speed has changed
     */
    private boolean updateSpeed() {
        double oldSpeed = speed;

        if (targetSpeed != speed || moveDist > 0) {
            long t1 = System.currentTimeMillis();
            long deltaT = t1 - t0;

            if (moveDist > 0) {
                double deltaD = speed * deltaT / 1000;
                moveDist -= deltaD;

                if (profile.isNeedToStop(speed, moveDist)) {
                    setTargetSpeed(0);
                }
            }

            if (targetSpeed != speed) {
                double a = targetSpeed > speed ? profile.getAccRate() : profile.getDecRate();
                speed += a * deltaT / 1000;
                speed = Math.min(Math.max(speed, 0), profile.getMaxSpeed());
            }

            t0 = t1;
        }

        return oldSpeed != speed;
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
                        ThrottleControlThread.getInstance().
                                addTask(address, new ThrottleControlThread.ThrottleControlTask(dccThrottle, throttle));
                        throttleOverride = false;
                    } else if (updateSpeed()) {
                        ThrottleControlThread.getInstance().
                                addTask(address, new ThrottleControlThread.ThrottleControlTask(dccThrottle, profile.getThrottle(speed)));
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
