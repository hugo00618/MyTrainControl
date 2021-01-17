package info.hugoyu.mytraincontrol.command.impl;

import info.hugoyu.mytraincontrol.command.ICommand;
import info.hugoyu.mytraincontrol.exception.CommandInvalidUsageException;
import info.hugoyu.mytraincontrol.util.TrainUtil;

public class MoveCommand implements ICommand {

    @Override
    public void execute(String[] args) throws Exception {
        try {
            int address = Integer.parseInt(args[1]);
            String stationId = args[2];
            TrainUtil.moveTo(address, stationId);
        } catch (NumberFormatException e) {
            throw new CommandInvalidUsageException(this);
        }
    }

    @Override
    public String argList() {
        return "{address} {stationId}";
    }

    @Override
    public int numberOfArgs() {
        return 3;
    }
}
