package info.hugoyu.mytraincontrol.command.impl;

import info.hugoyu.mytraincontrol.command.Command;
import info.hugoyu.mytraincontrol.trainset.Trainset;
import info.hugoyu.mytraincontrol.util.TrainUtil;
import lombok.extern.log4j.Log4j;

@Log4j
public class AllocateCommand implements Command {

    private int address;
    private long trackNodeId;

    @Override
    public boolean parseArgs(String[] args) {
        try {
            address = Integer.parseInt(args[1]);
            trackNodeId = Long.parseLong(args[2]);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    @Override
    public void execute() {
        Trainset trainset = TrainUtil.getTrainset(address);
        if (TrainUtil.allocateStationTrackImmediately(address, trackNodeId)) {
            System.out.println(trainset.getName() + ": allocation succeeded for track node " + trackNodeId);
        } else {
            System.err.println(trainset.getName() + ": allocation failed for track node " + trackNodeId);
        }
    }

    @Override
    public String[] expectedArgs() {
        return new String[]{"address", "trackNodeId"};
    }

}
