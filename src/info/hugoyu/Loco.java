package info.hugoyu;

import jmri.*;

public class Loco implements ThrottleListener {
    public static final double ACC_RATE_COEF = 45;
    public static final double DEC_RATE_COEF = 45;

    private String name;
    private LocoProfile profile;
    private Thread controlThread;
    private boolean running = false;

    private Object speedLock = new Object();
    private double speed = 0;

    private long t0;
    private double targetSpeed;

    private double moveDist = 0;

    public Loco(int address, String name, LocoProfile profile) {
        this.name = name;
        this.profile = profile;
        InstanceManager.getNullableDefault(ThrottleManager.class).requestThrottle(address, this);
    }

    public void stop() {
        moveDist = 0;
        setTargetSpeed(0);
    }

    public void move(double dist) {
        move(dist, profile.getMaxSpeed());
    }

    public void move(double dist, double targetSpeed) {
        moveDist = dist;
        setTargetSpeed(targetSpeed);
    }

    private void setTargetSpeed(double targetSpeed) {
        synchronized (speedLock) {
            t0 = System.currentTimeMillis();
            this.targetSpeed = targetSpeed;
            speedLock.notify();
        }
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
                System.out.println(profile.isNeedToStop(speed, moveDist));
            }

            if (targetSpeed != speed) {
                double a = targetSpeed > speed ? profile.getAccRate() : profile.getDecRate();
                speed += a * deltaT / 1000;
                speed = Math.min(Math.max(speed, 0), profile.getMaxSpeed());
            }

//            System.out.println(speed + " " + targetSpeed + " " + moveDist);

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
                    synchronized (speedLock) {
                        if (System.currentTimeMillis() - lastTrackedSliderMovementTime >= trackSliderMinInterval) {
                            lastTrackedSliderMovementTime = System.currentTimeMillis();

                            // check acc/dec
                            boolean speedChanged = updateSpeed();

                            dccThrottle.setSpeedSetting((float) (profile.getThrottleByte(speed) / 128));

                            // sleep if no further speed adjustment needed
//                            if (!speedChanged) {
//                                try {
//                                    speedLock.wait();
//                                } catch (InterruptedException e) {
//
//                                }
//                            }
                        }
                    }
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
        synchronized (speedLock) {
            running = false;
            speedLock.notify();
        }
    }
}
