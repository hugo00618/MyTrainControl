package info.hugoyu.mytraincontrol.commands;

import info.hugoyu.mytraincontrol.exceptions.CommandInvalidUsageException;
import info.hugoyu.mytraincontrol.util.TrainUtil;

public class LightControlCommand implements ICommand {
    private static final String LIGHT_STATUS_OFF = "off";
    private static final String LIGHT_STATUS_ON = "on";

    @Override
    public void execute(String[] args) throws Exception {
        try {
            int address = Integer.parseInt(args[1]);
            String lightStatus = args[2].toLowerCase();
            if (lightStatus.equals(LIGHT_STATUS_OFF)) {
                TrainUtil.setLight(address, false);
            } else if (lightStatus.equals(LIGHT_STATUS_ON)) {
                TrainUtil.setLight(address, true);
            } else {
                throw new CommandInvalidUsageException(this);
            }
        } catch (NumberFormatException e) {
            throw new CommandInvalidUsageException(this);
        }
    }

    @Override
    public String help() {
        return "l {address} {on/off}";
    }

    @Override
    public int numberOfArgs() {
        return 3;
    }
}
