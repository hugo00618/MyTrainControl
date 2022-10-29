package info.hugoyu.mytraincontrol.command.debug.impl;

import info.hugoyu.mytraincontrol.command.debug.AbstractDebugCommand;
import info.hugoyu.mytraincontrol.trainset.Trainset;
import info.hugoyu.mytraincontrol.util.TrainUtil;

public class SetThrottleCommand extends AbstractDebugCommand {

    @Override
    public void executeCommand(String[] args) {
        int address = Integer.parseInt(args[1]);
        int throttlePercent = Integer.parseInt(args[2]);
        Trainset trainset = TrainUtil.getTrainset(address);
        TrainUtil.setThrottle(trainset, throttlePercent);
    }

    @Override
    public String[] expectedArgs() {
        return new String[]{"address", "throttle[-100,100]"};
    }
}
