package info.hugoyu.mytraincontrol.commandstation.task.impl;

import info.hugoyu.mytraincontrol.commandstation.task.AbstractCommandStationTask;
import jmri.DccThrottle;

public class SetThrottleTask extends AbstractCommandStationTask {

    private DccThrottle throttle;
    private int throttlePercent;

    public SetThrottleTask(DccThrottle throttle, int throttlePercent) {
        super();

        this.throttlePercent = throttlePercent;
        this.throttle = throttle;
    }

    public SetThrottleTask(DccThrottle throttle, int throttlePercent, long delay) {
        super(System.currentTimeMillis(), delay);

        this.throttlePercent = throttlePercent;
        this.throttle = throttle;
    }

    @Override
    public void execute() {
        boolean isForward = throttlePercent >= 0;
        throttle.setIsForward(isForward);
        throttle.setSpeedSetting((float) (Math.abs(throttlePercent) / 100.0));
    }
}
