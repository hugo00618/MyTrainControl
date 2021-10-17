package info.hugoyu.mytraincontrol.command.impl;

import info.hugoyu.mytraincontrol.command.Command;
import info.hugoyu.mytraincontrol.util.TurnoutUtil;
import info.hugoyu.mytraincontrol.util.TurnoutUtil.TurnoutState;

public class TurnoutControlCommand implements Command {

    private static final String TURNOUT_STATE_THROWN = "thrown";
    private static final String TURNOUT_STATE_CLOSED = "closed";

    private int address;
    private String turnoutStateStr;

    @Override
    public boolean parseArgs(String[] args) {
        try {
            address = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            return false;
        }

        String turnoutState = args[2].toLowerCase();
        if (!turnoutState.equals(TURNOUT_STATE_THROWN) && !turnoutState.equals(TURNOUT_STATE_CLOSED)) {
            return false;
        }
        this.turnoutStateStr = turnoutState;

        return true;
    }

    @Override
    public void execute() {
        TurnoutState turnoutState = turnoutStateStr.equals(TURNOUT_STATE_THROWN) ?
                TurnoutState.THROWN : TurnoutState.CLOSED;
        TurnoutUtil.setTurnoutState(String.valueOf(address), turnoutState);
    }

    @Override
    public String[] expectedArgs() {
        return new String[]{"address", "thrown/closed"};
    }
}
