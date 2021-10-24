package info.hugoyu.mytraincontrol.command.impl;

import info.hugoyu.mytraincontrol.command.Command;
import info.hugoyu.mytraincontrol.util.BaseStationPowerUtil;
import jmri.JmriException;

public class EmergencyKillCommand implements Command {

    @Override
    public boolean execute(String[] args) {
        try {
            BaseStationPowerUtil.turnOffPower();
        } catch (JmriException e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    @Override
    public String[] expectedArgs() {
        return new String[0];
    }

}
