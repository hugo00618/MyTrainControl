package info.hugoyu;

import jmri.*;

public class Loco implements ThrottleListener {
    private static final float ACC_RATE = 0.08f;     // acceleration rate
    private static final float DEC_RATE = -0.08f;     // standard deceleration rate
    private static final float EDEC_RATE = -0.11f;    // emergency deceleration rate

    private static final float TOP_SPEED = 1.0f;

    private String name;
    private Thread controlThread;
    private boolean running = false;

    private float speed = 0f;

    private int accDecFlag = 0;
    private long accDecT0;
    private float accDecRate;
    private float targetSpeed;

    public Loco(int address, String name) {
        this.name = name;
        InstanceManager.getNullableDefault(ThrottleManager.class).requestThrottle(address, this);
    }

    /**
     * @param speed set loco to certain speed with acc/dcc
     */
    public void setSpeed(float speed) {
        if (speed > this.speed) {
            accelerate(speed);
        } else {
            decelerate(speed);
        }
    }

    /**
     * @param targetSpeed accelerate until reaches targetSpeed
     */
    public void accelerate(float targetSpeed) {
        accDecT0 = System.currentTimeMillis();
        accDecFlag = 1;
        accDecRate = ACC_RATE;
        this.targetSpeed = targetSpeed;
    }

    /**
     * @param targetSpeed decelerate to targetSpeed with standard deceleration rate
     */
    public void decelerate(float targetSpeed) {
        accDecT0 = System.currentTimeMillis();
        accDecFlag = 2;
        accDecRate = DEC_RATE;
        this.targetSpeed = targetSpeed;
    }

    /**
     * @param targetSpeed decelerate to targetSpeed with emergency deceleration rate
     */
    public void emergencyDecelerate(float targetSpeed) {
        accDecT0 = System.currentTimeMillis();
        accDecFlag = 2;
        accDecRate = EDEC_RATE;
        this.targetSpeed = targetSpeed;
    }

    @Override
    public void notifyStealThrottleRequired(LocoAddress locoAddress) {

    }

    @Override
    public void notifyThrottleFound(DccThrottle dccThrottle) {
        System.out.println(name + ": throttle found");

        controlThread = new Thread(new Runnable() {
            long lastTrackedSliderMovementTime = 0;
            static final long trackSliderMinInterval = 200;

            @Override
            public void run() {
//                TODO: eliminate spinning
                while (running) {
                    if (System.currentTimeMillis() - lastTrackedSliderMovementTime >= trackSliderMinInterval) {
                        lastTrackedSliderMovementTime = System.currentTimeMillis();

                        // check accelerate
                        if (accDecFlag != 0) {
                            if ((accDecFlag == 1 && speed > targetSpeed) ||
                                    (accDecFlag == 2 && speed < targetSpeed)) { // finish acc/dcc
                                accDecFlag = 0;
                            } else {
                                long accDecT1 = System.currentTimeMillis();
                                long deltaT = accDecT1 - accDecT0;
                                speed += accDecRate * deltaT / 1000;
                                accDecT0 = accDecT1;
                            }
                        }

                        dccThrottle.setSpeedSetting(speed);

                        try {
                            Thread.sleep(trackSliderMinInterval);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
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
        running = false;
    }
}
