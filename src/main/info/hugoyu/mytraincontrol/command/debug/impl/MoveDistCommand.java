package info.hugoyu.mytraincontrol.command.debug.impl;

import info.hugoyu.mytraincontrol.command.debug.AbstractDebugCommand;
import info.hugoyu.mytraincontrol.util.TrainUtil;

public class MoveDistCommand extends AbstractDebugCommand {

    @Override
    public boolean executeCommand(String[] args) {
        try {
            int address = Integer.parseInt(args[1]);
            int dist = Integer.parseInt(args[2]);
            TrainUtil.moveDist(address, dist);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    @Override
    public String[] expectedArgs() {
        return new String[]{"address", "distance"};
    }
}
