package info.hugoyu.mytraincontrol.command.impl;

import info.hugoyu.mytraincontrol.command.Command;
import info.hugoyu.mytraincontrol.util.TrainUtil;

public class ResetTotalDistCommand implements Command {
    @Override
    public void execute(String[] args) throws Exception {
        try {
            int address = Integer.parseInt(args[1]);
            TrainUtil.getTrainset(address).resetTotalMovedDist();
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String[] expectedArgs() {
        return new String[]{"address"};
    }

}
