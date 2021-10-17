package info.hugoyu.mytraincontrol.command.impl;

import info.hugoyu.mytraincontrol.command.Command;
import info.hugoyu.mytraincontrol.util.BaseStationPowerUtil;
import jmri.JmriException;

public class EmergencyKillCommand implements Command {

    @Override
    public boolean parseArgs(String[] args) {
        return true;
    }

    @Override
    public void execute() {
        try {
            BaseStationPowerUtil.turnOffPower();
        } catch (JmriException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String[] expectedArgs() {
        return new String[0];
    }

}
