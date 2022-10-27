package info.hugoyu.mytraincontrol.commandstation.task.impl;

import info.hugoyu.mytraincontrol.commandstation.task.AbstractCommandStationTask;
import info.hugoyu.mytraincontrol.turnout.Turnout;
import jmri.InstanceManager;
import jmri.TurnoutManager;

public class TurnoutControlTask extends AbstractCommandStationTask {

    private int address;
    private Turnout.State state;

    public TurnoutControlTask(int address, Turnout.State state) {
        super();

        this.address = address;
        this.state = state;
    }

    @Override
    public void execute() {
        jmri.Turnout turnout = InstanceManager.getDefault(TurnoutManager.class).provideTurnout(String.valueOf(address));
        turnout.setCommandedState(state.stateCode);
    }
}
