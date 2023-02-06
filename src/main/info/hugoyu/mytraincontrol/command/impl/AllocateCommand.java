package info.hugoyu.mytraincontrol.command.impl;

import info.hugoyu.mytraincontrol.command.Command;
import info.hugoyu.mytraincontrol.layout.node.impl.StationTrackNode;
import info.hugoyu.mytraincontrol.trainset.Trainset;
import info.hugoyu.mytraincontrol.util.AllocateUtil;
import info.hugoyu.mytraincontrol.util.LayoutUtil;
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
        StationTrackNode stationTrackNode = LayoutUtil.getStationTrackNode(trackNodeId0, trackNodeId1);
        String stationName = stationTrackNode.getStation().getName();

        if (AllocateUtil.reserveStationTrack(stationTrackNode, trainset)) {
            System.out.printf("%s: allocation succeeded for %s %s%n",
                    trainset.getName(),
                    stationName,
                    stationTrackNode.getName());
        } else {
            System.out.printf("%s: allocation failed for %s %s%n",
                    trainset.getName(),
                    stationName,
                    stationTrackNode.getName());
        }
    }

    @Override
    public String[] expectedArgs() {
        return new String[]{"address", "stationTrackNodeId0", "stationTrackNodeId1"};
    }

}
