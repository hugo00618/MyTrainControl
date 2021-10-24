package info.hugoyu.mytraincontrol.command.impl;

import info.hugoyu.mytraincontrol.command.Command;
import info.hugoyu.mytraincontrol.util.TrainUtil;

public class LightControlCommand implements Command {

    private static final String LIGHT_STATE_OFF = "off";
    private static final String LIGHT_STATE_ON = "on";

    @Override
    public boolean execute(String[] args) {
        int address;
        try {
            address = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            return false;
        }
        String lightStatus = args[2].toLowerCase();
        switch (lightStatus) {
            case LIGHT_STATE_OFF:
                TrainUtil.setLight(address, false);
                break;
            case LIGHT_STATE_ON:
                TrainUtil.setLight(address, true);
                break;
            default:
                return false;
        }

        return true;
    }

    @Override
    public String[] expectedArgs() {
        return new String[]{"address", "on/off"};
    }

}
