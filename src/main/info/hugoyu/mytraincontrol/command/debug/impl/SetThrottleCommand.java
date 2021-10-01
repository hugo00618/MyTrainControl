package info.hugoyu.mytraincontrol.command.debug.impl;

import info.hugoyu.mytraincontrol.command.debug.AbstractDebugCommand;
import info.hugoyu.mytraincontrol.registry.ThrottleRegistry;

public class SetThrottleCommand extends AbstractDebugCommand {

    @Override
    public void executeCommand(String[] args) {
        int address = Integer.parseInt(args[1]);
        double throttle = Integer.parseInt(args[2]);
        ThrottleRegistry.getInstance().getThrottle(address).setSpeedSetting((float) (throttle / 100.0));
    }

    @Override
    public String[] expectedArgs() {
        return new String[]{"throttle:0-100"};
    }
}
