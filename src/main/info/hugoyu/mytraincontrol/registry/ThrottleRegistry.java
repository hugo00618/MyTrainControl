package info.hugoyu.mytraincontrol.registry;

import info.hugoyu.mytraincontrol.util.ThrottleRetriever;
import info.hugoyu.mytraincontrol.util.ThrottleRetrieverListener;
import jmri.DccThrottle;

import java.util.HashMap;
import java.util.Map;

public class ThrottleRegistry implements ThrottleRetrieverListener {

    private static ThrottleRegistry instance;

    private Map<Integer, DccThrottle> throttles;

    private ThrottleRegistry() {
        throttles = new HashMap<>();
    }

    public static ThrottleRegistry getInstance() {
        if (instance == null) {
            instance = new ThrottleRegistry();
        }
        return instance;
    }

    public void registerThrottle(int address) {
        new ThrottleRetriever(address, this).requestThrottle();
    }

    @Override
    public void notifyThrottleFound(int address, DccThrottle throttle) {
        throttles.put(address, throttle);
    }

    public DccThrottle getThrottle(int address) {
        return throttles.get(address);
    }
}
