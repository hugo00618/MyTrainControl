package info.hugoyu;

import jmri.*;

import java.util.concurrent.locks.ReentrantLock;

public class Loco implements ThrottleListener {
    private String name;
    private LocoProfile profile;
    private Thread controlThread;
    private boolean running = false;

    private boolean throttleOverride = false;
    private float throttle;

    private ReentrantLock speedLock = new ReentrantLock();
    private double speed = 0;

    private long t0;
    private double targetSpeed;

    private double moveDist = 0;

    public Loco(int address, String name, LocoProfile profile) {
        this.name = name;
        this.profile = profile;
        InstanceManager.getNullableDefault(ThrottleManager.class).requestThrottle(address, this);
    }

    public void setThrottle(float throttle) {
        speedLock.lock();
        throttleOverride = true;
        this.throttle = throttle;
        speedLock.unlock();
    }

    public void stop() {
        speedLock.lock();
        moveDist = 0;
        throttleOverride = false;
        setTargetSpeed(0);
        speedLock.unlock();
    }

    public void move(double dist) {
        move(dist, profile.getMaxSpeed());
    }

    public void move(double dist, double targetSpeed) {
        speedLock.lock();
        moveDist = dist;
        setTargetSpeed(targetSpeed);
        speedLock.unlock();
    }

    public void setTargetSpeed(double targetSpeed) {
        boolean heldLock = speedLock.isHeldByCurrentThread();

        if (!heldLock) speedLock.lock();
        this.targetSpeed = targetSpeed;
        t0 = System.currentTimeMillis();
        if (!heldLock) speedLock.unlock();
    }

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
                } else {
                    setTargetSpeed(profile.getMaxSpeed());
                }
            }

            if (targetSpeed != speed) {
                double a = targetSpeed > speed ? profile.getAccRate() : profile.getDecRate();
                speed += a * deltaT / 1000;
                speed = Math.min(Math.max(speed, 0), profile.getMaxSpeed());
            }

            t0 = t1;
        }

        return speed != oldSpeed;
    }

    @Override
    public void notifyStealThrottleRequired(LocoAddress locoAddress) {

    }

    @Override
    public void notifyThrottleFound(DccThrottle dccThrottle) {
        System.out.println(name + ": throttle found");

        controlThread = new Thread(new Runnable() {
            long lastTrackedSliderMovementTime = System.currentTimeMillis();
            static final long trackSliderMinInterval = 200;

            @Override
            public void run() {
                while (running) {
                    speedLock.lock();
                    updateSpeed();

                    if (System.currentTimeMillis() - lastTrackedSliderMovementTime >= trackSliderMinInterval) {
                        lastTrackedSliderMovementTime = System.currentTimeMillis();

                        if (throttleOverride) {
                            dccThrottle.setSpeedSetting(throttle);
                        } else {
                            dccThrottle.setSpeedSetting((float) (profile.getThrottleByte(speed) / 128));
                        }

                        // sleep if no further speed adjustment needed
//                            if (!speedChanged) {
//                                try {
//                                    speedLock.wait();
//                                } catch (InterruptedException e) {
//
//                                }
//                            }
                    }
                    speedLock.unlock();
                }
            }
        });

        running = true;
        controlThread.start();
    }

    @Override
    public void notifyFailedThrottleRequest(LocoAddress locoAddress, String s) {

    }

    @Override
    public void notifyDecisionRequired(LocoAddress locoAddress, DecisionType decisionType) {

    }

    public void stopControlThread() {
        running = false;
    }
}
