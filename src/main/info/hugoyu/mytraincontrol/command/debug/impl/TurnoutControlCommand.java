package info.hugoyu.mytraincontrol.command.debug.impl;

import info.hugoyu.mytraincontrol.command.debug.AbstractDebugCommand;
import info.hugoyu.mytraincontrol.turnout.Turnout;
import info.hugoyu.mytraincontrol.util.TurnoutUtil;

public class TurnoutControlCommand extends AbstractDebugCommand {

    private static final String TURNOUT_STATE_THROWN = "thrown";
    private static final String TURNOUT_STATE_CLOSED = "closed";

    @Override
    public boolean executeCommand(String[] args) {
        int address;
        try {
            address = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            return false;
        }

        String turnoutStateStr = args[2].toLowerCase();
        Turnout.State turnoutState;
        switch (turnoutStateStr) {
            case TURNOUT_STATE_THROWN:
                turnoutState = Turnout.State.THROWN;
                break;
            case TURNOUT_STATE_CLOSED:
                turnoutState = Turnout.State.CLOSED;
                break;
            default:
                return false;
        }

        TurnoutUtil.setTurnoutState(address, turnoutState, true);
        return true;
    }

    @Override
    public String[] expectedArgs() {
        return new String[]{"address", "thrown/closed"};
    }
}
