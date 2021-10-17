package info.hugoyu.mytraincontrol.commandstation.task.impl;

import info.hugoyu.mytraincontrol.commandstation.task.AbstractCommandStationTask;
import info.hugoyu.mytraincontrol.util.TurnoutUtil.TurnoutState;
import jmri.InstanceManager;
import jmri.Turnout;
import jmri.TurnoutManager;

public class TurnoutControlTask extends AbstractCommandStationTask {

    private String address;
    private TurnoutState state;

    public TurnoutControlTask(String address, TurnoutState state) {
        super();

        this.address = address;
        this.state = state;
    }

    @Override
    public void execute() {
        Turnout turnout = InstanceManager.getDefault(TurnoutManager.class).provideTurnout(address);
        turnout.setCommandedState(state.stateCode);
    }
}
