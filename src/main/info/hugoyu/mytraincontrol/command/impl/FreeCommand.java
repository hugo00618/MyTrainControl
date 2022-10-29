package info.hugoyu.mytraincontrol.command.impl;

import info.hugoyu.mytraincontrol.command.Command;
import info.hugoyu.mytraincontrol.trainset.Trainset;
import info.hugoyu.mytraincontrol.util.TrainUtil;

public class FreeCommand implements Command {

    @Override
    public void execute(String[] args) {
        int address = Integer.parseInt(args[1]);
        Trainset trainset = TrainUtil.getTrainset(address);
        TrainUtil.freeAllAllocatedNodes(trainset);
    }

    @Override
    public String[] expectedArgs() {
        return new String[]{"address"};
    }
}
