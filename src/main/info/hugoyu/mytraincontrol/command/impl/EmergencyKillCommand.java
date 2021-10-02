package info.hugoyu.mytraincontrol.command.impl;

import info.hugoyu.mytraincontrol.command.Command;
import info.hugoyu.mytraincontrol.exception.CommandInvalidUsageException;
import info.hugoyu.mytraincontrol.util.BaseStationPowerUtil;
import jmri.JmriException;

public class EmergencyKillCommand implements Command {
    @Override
    public void execute(String[] args) throws CommandInvalidUsageException, JmriException {
        BaseStationPowerUtil.turnOffPower();
    }

    @Override
    public String[] expectedArgs() {
        return new String[0];
    }

}
