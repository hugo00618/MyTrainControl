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
        int sensor1 = Integer.parseInt(args[2]);
        int sensor2 = Integer.parseInt(args[3]);
        int sectionLength = Integer.parseInt(args[4]);
        int startThrottle = Integer.parseInt(args[5]);
        int endThrottle = Integer.parseInt(args[6]);
        int step = Integer.parseInt(args[7]);
        boolean isStartingForward = Boolean.parseBoolean(args[8]);

        Trainset trainset = TrainUtil.getTrainset(address);

        new Thread(() -> {
            try {
                SpeedProfilingUtil.profileSpeed(trainset, sensor1, sensor2, sectionLength,
                        startThrottle, endThrottle, step, isStartingForward);
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException("Error running command");
            }
        }).start();
    }

    @Override
    public String[] expectedArgs() {
        return new String[]{"address", "sensorPin1", "sensorPin2", "sectionLength(mm)",
                "startThrottle(1,100]", "endThrottle(1, 100]", "step", "isStartingForward"};
    }

}
