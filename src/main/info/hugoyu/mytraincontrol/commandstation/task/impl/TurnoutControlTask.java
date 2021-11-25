package info.hugoyu.mytraincontrol.commandstation.task.impl;

import info.hugoyu.mytraincontrol.commandstation.task.AbstractCommandStationTask;
import info.hugoyu.mytraincontrol.util.TurnoutState;
import jmri.InstanceManager;
import jmri.Turnout;
import jmri.TurnoutManager;

public class TurnoutControlTask extends AbstractCommandStationTask {

    private int address;
    private TurnoutState state;

    public TurnoutControlTask(int address, TurnoutState state) {
        super();

        this.address = address;
        this.state = state;
    }

    @Override
    public void execute() {
        Turnout turnout = InstanceManager.getDefault(TurnoutManager.class).provideTurnout(String.valueOf(address));
        turnout.setCommandedState(state.stateCode);
    }
}
