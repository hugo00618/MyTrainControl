package info.hugoyu.mytraincontrol.command.debug.impl;

import info.hugoyu.mytraincontrol.command.debug.AbstractDebugCommand;
import info.hugoyu.mytraincontrol.exception.CommandInvalidUsageException;
import info.hugoyu.mytraincontrol.util.TrainUtil;

public class MoveDistCommand extends AbstractDebugCommand {

    @Override
    public void executeCommand(String[] args) throws Exception {
        try {
            int address = Integer.parseInt(args[1]);
            int dist = Integer.parseInt(args[2]);
            TrainUtil.moveDist(address, dist);
        } catch (NumberFormatException e) {
            throw new CommandInvalidUsageException(this);
        }
    }

    @Override
    public String argList() {
        return "{address} {distance}";
    }

    @Override
    public int numberOfArgs() {
        return 3;
    }
}
