package info.hugoyu.mytraincontrol.command.debug.impl;

import info.hugoyu.mytraincontrol.command.debug.AbstractDebugCommand;
import info.hugoyu.mytraincontrol.trainset.Trainset;
import info.hugoyu.mytraincontrol.util.TrainUtil;

public class MoveDistCommand extends AbstractDebugCommand {

    @Override
    public void executeCommand(String[] args) {
        int address = Integer.parseInt(args[1]);
        int dist = Integer.parseInt(args[2]);
        Trainset trainset = TrainUtil.getTrainset(address);
        TrainUtil.moveDist(trainset, dist);
    }

    @Override
    public String[] expectedArgs() {
        return new String[]{"address", "distance"};
    }
}
