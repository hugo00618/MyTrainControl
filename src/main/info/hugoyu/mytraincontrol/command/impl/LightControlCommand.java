package info.hugoyu.mytraincontrol.command.impl;

import info.hugoyu.mytraincontrol.command.Command;
import info.hugoyu.mytraincontrol.util.TrainUtil;

public class LightControlCommand implements Command {

    private static final String LIGHT_STATE_OFF = "off";
    private static final String LIGHT_STATE_ON = "on";

    private int address;
    private String lightStatus;

    @Override
    public boolean parseArgs(String[] args) {
        try {
            address = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            return false;
        }

        String lightStatus = args[2].toLowerCase();
        if (!lightStatus.equals(LIGHT_STATE_OFF) && !lightStatus.equals(LIGHT_STATE_ON)) {
            return false;
        }
        this.lightStatus = lightStatus;

        return true;
    }

    @Override
    public void execute() {
        if (lightStatus.equals(LIGHT_STATE_OFF)) {
            TrainUtil.setLight(address, false);
        } else { // LIGHT_STATE_ON
            TrainUtil.setLight(address, true);
        }
    }

    @Override
    public String[] expectedArgs() {
        return new String[]{"address", "on/off"};
    }

}
