package info.hugoyu.mytraincontrol.command.debug.impl;

import info.hugoyu.mytraincontrol.command.debug.AbstractDebugCommand;
import info.hugoyu.mytraincontrol.registry.TurnoutRegistry;
import info.hugoyu.mytraincontrol.turnout.Turnout;
import info.hugoyu.mytraincontrol.util.TurnoutUtil;

public class TurnoutControlCommand extends AbstractDebugCommand {

    @Override
    public void executeCommand(String[] args) {
        int address = Integer.parseInt(args[1]);
        Turnout turnout = TurnoutRegistry.getInstance().getTurnout(address);

        String turnoutStateStr = args[2].toUpperCase();
        Turnout.State turnoutState = Turnout.State.valueOf(turnoutStateStr);

        TurnoutUtil.setTurnoutState(turnout, turnoutState, true);
    }

    @Override
    public String[] expectedArgs() {
        return new String[]{"address", "thrown/closed"};
    }
}
