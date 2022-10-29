package info.hugoyu.mytraincontrol.command.impl;

import info.hugoyu.mytraincontrol.command.Command;
import info.hugoyu.mytraincontrol.trainset.Trainset;
import info.hugoyu.mytraincontrol.util.LightState;
import info.hugoyu.mytraincontrol.util.TrainUtil;

public class LightControlCommand implements Command {
    
    @Override
    public void execute(String[] args) {
        int address = Integer.parseInt(args[1]);
        Trainset trainset = TrainUtil.getTrainset(address);

        String lightStateStr = args[2].toUpperCase();
        LightState lightState = LightState.valueOf(lightStateStr);

        TrainUtil.setLight(trainset, lightState);
    }

    @Override
    public String[] expectedArgs() {
        return new String[]{"address", "on/off"};
    }

}
