package info.hugoyu.mytraincontrol.commands;

import info.hugoyu.mytraincontrol.registries.ThrottleRegistry;
import jmri.Throttle;

/**
 * For debug only
 */
public class SetThrottleCommand implements ICommand {
    @Override
    public void execute(String[] args) throws Exception {
        int address = Integer.parseInt(args[1]);
        double throttle = Integer.parseInt(args[2]);
        ThrottleRegistry.getInstance().getThrottle(address).setSpeedSetting((float) (throttle / 100.0));
    }

    @Override
    public String help() {
        return null;
    }

    @Override
    public int numberOfArgs() {
        return 3;
    }
}
