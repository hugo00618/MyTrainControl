package info.hugoyu.mytraincontrol.command.debug.impl;

import info.hugoyu.mytraincontrol.command.debug.AbstractDebugCommand;
import info.hugoyu.mytraincontrol.registry.ThrottleRegistry;

public class SetThrottleCommand extends AbstractDebugCommand {

    private int address;
    private double throttle;

    @Override
    public boolean parseArgs(String[] args) {
        try {
            address = Integer.parseInt(args[1]);
            throttle = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            return false;
        }

        return true;
    }

    @Override
    public void executeCommand() {
        ThrottleRegistry.getInstance().getThrottle(address).setSpeedSetting((float) (throttle / 100.0));
    }

    @Override
    public String[] expectedArgs() {
        return new String[]{"throttle:0-100"};
    }
}
