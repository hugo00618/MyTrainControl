package info.hugoyu.mytraincontrol.commands;

import info.hugoyu.mytraincontrol.util.BaseStationPowerUtil;

public class PowerOnCommand implements ICommand{
    @Override
    public void execute(String[] args) throws Exception {
        BaseStationPowerUtil.turnOnPower();
    }

    @Override
    public String help() {
        return null;
    }

    @Override
    public int numberOfArgs() {
        return 1;
    }
}
