package info.hugoyu.mytraincontrol.command.impl;

import info.hugoyu.mytraincontrol.command.Command;
import info.hugoyu.mytraincontrol.trainset.Trainset;
import info.hugoyu.mytraincontrol.util.LightState;
import info.hugoyu.mytraincontrol.util.TrainUtil;

public class RegisterCommand implements Command {

    @Override
    public void execute(String[] args) {
        int address = Integer.parseInt(args[1]);

        boolean isMotorReversed = false;
        if (args.length == 5) {
            isMotorReversed = Boolean.parseBoolean(args[4]);
        }

        String name = args[2];
        String profileFileName = args[3];

        TrainUtil.registerTrainset(address, name, profileFileName, isMotorReversed);
        Trainset trainset = TrainUtil.getTrainset(address);
        TrainUtil.setLight(trainset, LightState.ON);
        System.out.println(String.format("Trainset registered: %s at address %s", name, address));
    }

    @Override
    public String[] expectedArgs() {
        return new String[]{"address", "name", "profileFileName", "(isMotorReversed)"};
    }
}
