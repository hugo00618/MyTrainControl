package info.hugoyu.mytraincontrol.command.impl;

import info.hugoyu.mytraincontrol.command.Command;
import info.hugoyu.mytraincontrol.util.BaseStationPowerUtil;
import jmri.JmriException;

public class PowerControlCommand implements Command {

    private static final String POWER_STATE_OFF = "off";
    private static final String POWER_STATE_ON = "on";

    @Override
    public boolean execute(String[] args) {
        String powerStatus = args[1].toLowerCase();
        try {
            switch (powerStatus) {
                case POWER_STATE_OFF:
                    BaseStationPowerUtil.turnOffPower();
                    break;
                case POWER_STATE_ON:
                    BaseStationPowerUtil.turnOnPower();
                    break;
                default:
                    return false;
            }
        } catch (JmriException e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    @Override
    public String[] expectedArgs() {
        return new String[]{"on/off"};
    }

}
