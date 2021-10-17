package info.hugoyu.mytraincontrol.command.impl;

import info.hugoyu.mytraincontrol.command.Command;
import info.hugoyu.mytraincontrol.util.TrainUtil;

public class MoveCommand implements Command {

    private int address;
    private String stationId;

    @Override
    public boolean parseArgs(String[] args) {
        try {
            address = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            return false;
        }
        stationId = args[2];

        return true;
    }

    @Override
    public void execute() {
        TrainUtil.moveTo(address, stationId);
    }

    @Override
    public String[] expectedArgs() {
        return new String[]{"address", "stationId"};
    }

}
