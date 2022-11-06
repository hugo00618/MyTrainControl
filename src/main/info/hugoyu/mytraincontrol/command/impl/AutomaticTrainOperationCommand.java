package info.hugoyu.mytraincontrol.command.impl;

import info.hugoyu.mytraincontrol.command.Command;
import info.hugoyu.mytraincontrol.command.constant.OnOffState;
import info.hugoyu.mytraincontrol.trainset.Trainset;
import info.hugoyu.mytraincontrol.util.AutomaticTrainOperationUtil;
import info.hugoyu.mytraincontrol.util.TrainUtil;

public class AutomaticTrainOperationCommand implements Command {

    @Override
    public void execute(String[] args) {
        int address = Integer.parseInt(args[1]);
        Trainset trainset = TrainUtil.getTrainset(address);

        String powerStateStr = args[2].toUpperCase();
        OnOffState onOffState = OnOffState.valueOf(powerStateStr);

        switch (onOffState) {
            case ON:
                AutomaticTrainOperationUtil.enableAto(trainset);
                break;
            case OFF:
                AutomaticTrainOperationUtil.disableAto(trainset);
                break;
            default:
                // should not be here
                throw new RuntimeException();
        }
    }

    @Override
    public String[] expectedArgs() {
        return new String[] {"address", "on/off"};
    }
}
