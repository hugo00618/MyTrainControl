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
        int sensor = Integer.parseInt(args[2]);
        int sectionLength = Integer.parseInt(args[3]);
        int startThrottle = Integer.parseInt(args[4]);
        int endThrottle = Integer.parseInt(args[5]);
        int step = Integer.parseInt(args[6]);

        Trainset trainset = TrainUtil.getTrainset(address);

        new Thread(() -> {
            try {
                SpeedProfilingUtil.profileSpeed(trainset, sensor, sectionLength,
                        startThrottle, endThrottle, step);
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException("Error running command");
            }
        }).start();
    }

    @Override
    public String[] expectedArgs() {
        return new String[]{"address", "sensorPin", "sectionLength(mm)",
                "startThrottle(1,100]", "endThrottle(1, 100]", "step"};
    }

}
