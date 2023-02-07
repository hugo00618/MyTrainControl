package info.hugoyu.mytraincontrol.command.debug.impl;

import info.hugoyu.mytraincontrol.command.debug.AbstractDebugCommand;
import info.hugoyu.mytraincontrol.registry.switchable.impl.CrossoverRegistry;
import info.hugoyu.mytraincontrol.switchable.impl.Crossover;
import info.hugoyu.mytraincontrol.switchable.impl.Turnout;
import info.hugoyu.mytraincontrol.util.SwitchUtil;

public class CrossoverControlCommand extends AbstractDebugCommand {

    @Override
    public String[] expectedArgs() {
        return new String[]{"address", "thrown/closed"};
    }

    @Override
    public void executeCommand(String[] args) {
        int address = Integer.parseInt(args[1]);
        Crossover crossover = CrossoverRegistry.getInstance().getCrossover(address);

        String turnoutStateStr = args[2].toUpperCase();
        Turnout.State turnoutState = Turnout.State.valueOf(turnoutStateStr);

        SwitchUtil.setSwitchState(crossover, turnoutState, true, null);
    }
}
