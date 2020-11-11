package info.hugoyu.mytraincontrol.commands;

import info.hugoyu.mytraincontrol.exceptions.CommandInvalidUsageException;
import info.hugoyu.mytraincontrol.util.TrainUtil;

public class SetSpeedCommand implements  ICommand {
    @Override
    public void execute(String[] args) throws Exception {
        try {
            int address = Integer.parseInt(args[1]);
            double speed = Integer.parseInt(args[2]);
            TrainUtil.setSpeed(address, speed);
        } catch (NumberFormatException e) {
            throw new CommandInvalidUsageException(this);
        }
    }

    @Override
    public String help() {
        return "ss {address} {speed (double) 0 - inf}";
    }

    @Override
    public int numberOfArgs() {
        return 3;
    }
}
