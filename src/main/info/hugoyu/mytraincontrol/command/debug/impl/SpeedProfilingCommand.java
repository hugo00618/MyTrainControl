package info.hugoyu.mytraincontrol.command.debug.impl;

import info.hugoyu.mytraincontrol.command.debug.AbstractDebugCommand;
import info.hugoyu.mytraincontrol.trainset.Trainset;
import info.hugoyu.mytraincontrol.util.SpeedProfilingUtil;
import info.hugoyu.mytraincontrol.util.TrainUtil;

import java.util.concurrent.ExecutionException;

public class SpeedProfilingCommand extends AbstractDebugCommand {

    @Override
    public void executeCommand(String[] args) {
        int address = Integer.parseInt(args[1]);
        int startSensor = Integer.parseInt(args[2]);
        int stopSensor = Integer.parseInt(args[3]);
        int sectionLength = Integer.parseInt(args[4]);
        int startThrottle = Integer.parseInt(args[5]);
        int endThrottle = Integer.parseInt(args[6]);
        int step = Integer.parseInt(args[7]);

        Trainset trainset = TrainUtil.getTrainset(address);

        try {
            SpeedProfilingUtil.speedProfile(trainset, startSensor, stopSensor, sectionLength,
                    startThrottle, endThrottle, step);
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error running command");
        }
    }

    @Override
    public String[] expectedArgs() {
        return new String[]{"address", "startSensor", "stopSensor", "sectionLength(mm)",
                "startThrottle(1,100]", "endThrottle(1, 100]", "step"};
    }

}
