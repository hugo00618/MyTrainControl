package info.hugoyu.mytraincontrol.command.debug.impl;

import info.hugoyu.mytraincontrol.command.debug.AbstractDebugCommand;
import info.hugoyu.mytraincontrol.exception.InvalidIdException;
import info.hugoyu.mytraincontrol.registry.ThrottleRegistry;
import info.hugoyu.mytraincontrol.turnout.Turnout;
import info.hugoyu.mytraincontrol.util.CrossoverUtil;
import jmri.DccThrottle;

public class CrossoverControlCommand extends AbstractDebugCommand {
    @Override
    public String[] expectedArgs() {
        return new String[]{"address", "thrown/closed"};
    }

    @Override
    public void executeCommand(String[] args) {
        int address = Integer.parseInt(args[1]);
        try {
            ThrottleRegistry.getInstance().registerThrottle(address);
        } catch (InvalidIdException e) {
            // ignore if the crossover throttle is already registered
        }
        DccThrottle crossover = ThrottleRegistry.getInstance().getThrottle(address);

        String turnoutStateStr = args[2].toUpperCase();
        Turnout.State turnoutState = Turnout.State.valueOf(turnoutStateStr);

        CrossoverUtil.setTurnoutState(crossover, turnoutState, true);
    }
}
