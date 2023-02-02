package info.hugoyu.mytraincontrol.command.impl;

import info.hugoyu.mytraincontrol.command.Command;
import info.hugoyu.mytraincontrol.trainset.Trainset;
import info.hugoyu.mytraincontrol.util.TrainUtil;
import lombok.extern.log4j.Log4j;

@Log4j
public class AllocateCommand implements Command {

    @Override
    public void execute(String[] args) {
        int address = Integer.parseInt(args[1]);
        long trackNodeId0 = Long.parseLong(args[2]);
        long trackNodeId1 = Long.parseLong(args[3]);

        Trainset trainset = TrainUtil.getTrainset(address);

        if (TrainUtil.allocateStationTrackImmediately(trainset, trackNodeId0, trackNodeId1)) {
            System.out.println(trainset.getName() + ": allocation succeeded for track node " + trackNodeId0);
        } else {
            System.err.println(trainset.getName() + ": allocation failed for track node " + trackNodeId0);
        }
    }

    @Override
    public String[] expectedArgs() {
        return new String[]{"address", "trackNodeId0", "trackNodeId1"};
    }

}
