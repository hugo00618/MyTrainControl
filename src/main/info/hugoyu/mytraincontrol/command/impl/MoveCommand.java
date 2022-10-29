package info.hugoyu.mytraincontrol.command.impl;

import info.hugoyu.mytraincontrol.command.Command;
import info.hugoyu.mytraincontrol.trainset.Trainset;
import info.hugoyu.mytraincontrol.util.TrainUtil;

public class MoveCommand implements Command {

    @Override
    public void execute(String[] args) {
        int address = Integer.parseInt(args[1]);
        String stationId = args[2];
        Trainset trainset = TrainUtil.getTrainset(address);
        TrainUtil.moveTo(trainset, stationId);
    }

    @Override
    public String[] expectedArgs() {
        return new String[]{"address", "stationId"};
    }

}
