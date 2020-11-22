package info.hugoyu.mytraincontrol.commands;

import info.hugoyu.mytraincontrol.exceptions.CommandInvalidUsageException;
import info.hugoyu.mytraincontrol.util.TrainUtil;

public class MoveCommand implements ICommand {
    @Override
    public void execute(String[] args) throws Exception {
        try {
            int address = Integer.parseInt(args[1]);
            int dist = Integer.parseInt(args[2]);
            TrainUtil.move(address, dist);
        } catch (NumberFormatException e) {
            throw new CommandInvalidUsageException(this);
        }
    }

    @Override
    public String help() {
        return "mv {address} {distance}";
    }

    @Override
    public int numberOfArgs() {
        return 3;
    }
}
