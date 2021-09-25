package info.hugoyu.mytraincontrol.command.impl;

import info.hugoyu.mytraincontrol.command.Command;
import info.hugoyu.mytraincontrol.exception.CommandInvalidUsageException;
import info.hugoyu.mytraincontrol.registry.TrainsetRegistry;
import info.hugoyu.mytraincontrol.trainset.Trainset;
import info.hugoyu.mytraincontrol.util.TrainUtil;
import lombok.extern.log4j.Log4j;

@Log4j
public class AllocateCommand implements Command {

    @Override
    public void execute(String[] args) throws CommandInvalidUsageException {
        try {
            int address = Integer.parseInt(args[1]);
            long trackNodeId = Long.parseLong(args[2]);
            Trainset trainset = TrainsetRegistry.getInstance().getTrainset(address);
            if (TrainUtil.allocateStationTrackImmediate(address, trackNodeId)) {
                System.out.println(trainset.getName() + ": allocation succeeded for track node " + trackNodeId);
            } else {
                System.err.println(trainset.getName() + ": allocation failed for track node " + trackNodeId);
                System.err.println(trainset.getName() + ": allocation failed for track node " + trackNodeId);
            }
        } catch (NumberFormatException e) {
            throw new CommandInvalidUsageException(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String argList() {
        return "{address} {trackNodeId}";
    }

    @Override
    public int numberOfArgs() {
        return 3;
    }
}
