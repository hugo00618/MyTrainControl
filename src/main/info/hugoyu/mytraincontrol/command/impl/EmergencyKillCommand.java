package info.hugoyu.mytraincontrol.command.impl;

import info.hugoyu.mytraincontrol.command.Command;
import info.hugoyu.mytraincontrol.util.BaseStationPowerUtil;

public class EmergencyKillCommand implements Command {

    @Override
    public void execute(String[] args) {
        BaseStationPowerUtil.turnOffPower();
    }

    @Override
    public String[] expectedArgs() {
        return new String[0];
    }

}
