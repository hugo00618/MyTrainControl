package info.hugoyu.mytraincontrol.command.debug.impl;

import info.hugoyu.mytraincontrol.command.debug.AbstractDebugCommand;
import info.hugoyu.mytraincontrol.registry.switchable.impl.TurnoutRegistry;
import info.hugoyu.mytraincontrol.switchable.Switchable;
import info.hugoyu.mytraincontrol.switchable.impl.Turnout;
import info.hugoyu.mytraincontrol.util.SwitchUtil;

public class TurnoutControlCommand extends AbstractDebugCommand {

    @Override
    public void executeCommand(String[] args) {
        int address = Integer.parseInt(args[1]);
        Turnout turnout = TurnoutRegistry.getInstance().getTurnout(address);

        String switchStateStr = args[2].toUpperCase();
        Switchable.State switchState = Switchable.State.valueOf(switchStateStr);

        SwitchUtil.setSwitchState(turnout, switchState, true);
    }

    @Override
    public String[] expectedArgs() {
        return new String[]{"address", "thrown/closed"};
    }
}
