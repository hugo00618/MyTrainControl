package info.hugoyu.mytraincontrol.util;

import jmri.DccThrottle;

public interface ThrottleRetrieverListener {
    void notifyThrottleFound(int address, DccThrottle throttle);
}
