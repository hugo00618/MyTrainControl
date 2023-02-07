package info.hugoyu.mytraincontrol.commandstation.task.impl;

import info.hugoyu.mytraincontrol.commandstation.task.AbstractCommandStationTask;
import info.hugoyu.mytraincontrol.switchable.impl.Turnout;
import jmri.InstanceManager;
import jmri.TurnoutManager;

public class TurnoutControlTask extends AbstractCommandStationTask {

    private static final long HIGH_CURRENT_CONSUMPTION_PERIOD = 500;

    private int address;
    private Turnout.State state;

    public TurnoutControlTask(int address, Turnout.State state) {
        super();

        super.highCurrentConsumptionPeriod = HIGH_CURRENT_CONSUMPTION_PERIOD;

        this.address = address;
        this.state = state;
    }

    @Override
    public void execute() {
        jmri.Turnout turnout = InstanceManager.getDefault(TurnoutManager.class).provideTurnout(String.valueOf(address));
        turnout.setCommandedState(state.stateCode);
    }
}
