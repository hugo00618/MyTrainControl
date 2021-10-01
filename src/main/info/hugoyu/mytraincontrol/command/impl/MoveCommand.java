package info.hugoyu.mytraincontrol.command.impl;

import info.hugoyu.mytraincontrol.command.Command;
import info.hugoyu.mytraincontrol.exception.CommandInvalidUsageException;
import info.hugoyu.mytraincontrol.util.TrainUtil;

public class MoveCommand implements Command {

    @Override
    public void execute(String[] args) throws Exception {
        try {
            int address = Integer.parseInt(args[1]);
            String stationId = args[2];
            TrainUtil.moveTo(address, stationId);
        } catch (NumberFormatException e) {
            throw new CommandInvalidUsageException(this);
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String[] expectedArgs() {
        return new String[]{"address", "stationId"};
    }

}