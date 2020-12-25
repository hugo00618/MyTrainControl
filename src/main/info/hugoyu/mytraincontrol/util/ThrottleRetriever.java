package info.hugoyu.mytraincontrol.util;

import jmri.DccThrottle;
import jmri.InstanceManager;
import jmri.LocoAddress;
import jmri.ThrottleListener;
import jmri.ThrottleManager;

public class ThrottleRetriever implements ThrottleListener {

    private int address;
    private ThrottleRetrieverListener throttleRetrieverListener;

    public ThrottleRetriever(int address, ThrottleRetrieverListener throttleRetrieverListener) {
        this.address = address;
        this.throttleRetrieverListener = throttleRetrieverListener;
    }

    public void requestThrottle() {
        InstanceManager.getNullableDefault(ThrottleManager.class).requestThrottle(address, this);
    }

    @Override
    public void notifyStealThrottleRequired(LocoAddress locoAddress) {

    }

    @Override
    public void notifyThrottleFound(DccThrottle dccThrottle) {
        throttleRetrieverListener.notifyThrottleFound(address, dccThrottle);
    }

    @Override
    public void notifyFailedThrottleRequest(LocoAddress locoAddress, String s) {

    }

    @Override
    public void notifyDecisionRequired(LocoAddress locoAddress, DecisionType decisionType) {

    }
}
