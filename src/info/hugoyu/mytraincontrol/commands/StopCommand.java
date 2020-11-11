package info.hugoyu.mytraincontrol.commands;

import info.hugoyu.mytraincontrol.exceptions.CommandInvalidUsageException;
import info.hugoyu.mytraincontrol.util.TrainUtil;

public class StopCommand implements ICommand {

    @Override
    public void execute(String[] args) throws Exception {
        try {
            int address = Integer.parseInt(args[1]);
            TrainUtil.setSpeed(address, 0);
        } catch (NumberFormatException e) {
            throw new CommandInvalidUsageException(this);
        }
    }

    @Override
    public String help() {
        return "s {address}";
    }

    @Override
    public int numberOfArgs() {
        return 2;
    }
}
