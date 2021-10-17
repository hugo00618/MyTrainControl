package info.hugoyu.mytraincontrol.command.impl;

import info.hugoyu.mytraincontrol.command.Command;
import info.hugoyu.mytraincontrol.util.BaseStationPowerUtil;
import jmri.JmriException;

public class PowerControlCommand implements Command {

    private static final String POWER_STATE_OFF = "off";
    private static final String POWER_STATE_ON = "on";

    private String powerStatus;

    @Override
    public boolean parseArgs(String[] args) {
        String powerStatus = args[1].toLowerCase();
        if (!powerStatus.equals(POWER_STATE_OFF) && !powerStatus.equals(POWER_STATE_ON)) {
            return false;
        }
        this.powerStatus = powerStatus;

        return true;
    }

    @Override
    public void execute() {
        try {
            if (powerStatus.equals(POWER_STATE_OFF)) {
                BaseStationPowerUtil.turnOffPower();
            } else { // POWER_STATE_ON
                BaseStationPowerUtil.turnOnPower();
            }
        } catch (JmriException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String[] expectedArgs() {
        return new String[]{"on/off"};
    }

}
