package info.hugoyu.mytraincontrol.command.debug.impl;

import info.hugoyu.mytraincontrol.command.debug.AbstractDebugCommand;
import info.hugoyu.mytraincontrol.registry.ThrottleRegistry;

public class SetThrottleCommand extends AbstractDebugCommand {

    @Override
    public boolean executeCommand(String[] args) {
        try {
            int address = Integer.parseInt(args[1]);
            int throttle = Integer.parseInt(args[2]);
            ThrottleRegistry.getInstance().getThrottle(address).setSpeedSetting((float) (throttle / 100.0));
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    @Override
    public String[] expectedArgs() {
        return new String[]{"address", "throttle:0-100"};
    }
}
