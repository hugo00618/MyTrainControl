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
    public String argList() {
        return "{throttle:0-100}";
    }

    @Override
    public int numberOfArgs() {
        return 3;
    }
}
