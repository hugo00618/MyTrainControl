package info.hugoyu.mytraincontrol;


import jmri.DccThrottle;
import jmri.InstanceManager;
import jmri.LocoAddress;
import jmri.ThrottleListener;
import jmri.ThrottleManager;

public class Trainset implements ThrottleListener {
    String name;

    DccThrottle throttle;

    public Trainset(int address, String name) {
        this.name = name;
        InstanceManager.getNullableDefault(ThrottleManager.class).requestThrottle(address, this);
    }

    public void setSpeed(float speed) {
        throttle.setSpeedSetting(speed);
    }

    @Override
    public void notifyStealThrottleRequired(LocoAddress locoAddress) {

    }

    @Override
    public void notifyThrottleFound(DccThrottle dccThrottle) {
        System.out.println(name + ": throttle registered");
        this.throttle = dccThrottle;
    }

    @Override
    public void notifyFailedThrottleRequest(LocoAddress locoAddress, String s) {

    }

    @Override
    public void notifyDecisionRequired(LocoAddress locoAddress, DecisionType decisionType) {

    }
}
