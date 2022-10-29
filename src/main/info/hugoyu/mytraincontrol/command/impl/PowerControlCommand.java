package info.hugoyu.mytraincontrol.command.impl;

import info.hugoyu.mytraincontrol.command.Command;
import info.hugoyu.mytraincontrol.util.BaseStationPowerUtil;

public class PowerControlCommand implements Command {

    private enum PowerState {
        ON,
        OFF
    }

    @Override
    public void execute(String[] args) {
        String powerStateStr = args[1].toUpperCase();
        PowerState powerState = PowerState.valueOf(powerStateStr);

            switch (powerState) {
                case OFF:
                    BaseStationPowerUtil.turnOffPower();
                    break;
                case ON:
                    BaseStationPowerUtil.turnOnPower();
                    break;
                default:
                    // should not run to here
                    throw new RuntimeException();
            }

    }

    @Override
    public String[] expectedArgs() {
        return new String[]{"on/off"};
    }

}
