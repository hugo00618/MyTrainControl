package info.hugoyu.mytraincontrol.command.impl;

import info.hugoyu.mytraincontrol.command.Command;
import info.hugoyu.mytraincontrol.exception.CommandInvalidUsageException;
import info.hugoyu.mytraincontrol.util.BaseStationPowerUtil;
import jmri.JmriException;

public class PowerControlCommand implements Command {
    @Override
    public void execute(String[] args) throws CommandInvalidUsageException, JmriException {
        String powerStatus = args[1].toLowerCase();
        if (powerStatus.equals("on")) {
            BaseStationPowerUtil.turnOnPower();
        } else if (powerStatus.equals("off")) {
            BaseStationPowerUtil.turnOffPower();
        } else {
            throw new CommandInvalidUsageException(this);
        }
    }

    @Override
    public String argList() {
        return "{on/off}";
    }

    @Override
    public int numberOfArgs() {
        return 2;
    }
}
