package info.hugoyu.mytraincontrol.command.debug.impl;

import info.hugoyu.mytraincontrol.command.debug.AbstractDebugCommand;
import info.hugoyu.mytraincontrol.util.TrainUtil;

public class MoveDistCommand extends AbstractDebugCommand {

    private int address, dist;

    @Override
    public boolean parseArgs(String[] args) {
        try {
            address = Integer.parseInt(args[1]);
            dist = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            return false;
        }

        return true;
    }

    @Override
    public void executeCommand() {
        TrainUtil.moveDist(address, dist);
    }

    @Override
    public String[] expectedArgs() {
        return new String[]{"address", "distance"};
    }
}
