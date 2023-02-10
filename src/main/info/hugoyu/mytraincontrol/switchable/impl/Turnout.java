package info.hugoyu.mytraincontrol.switchable.impl;

import info.hugoyu.mytraincontrol.commandstation.task.AbstractCommandStationTask;
import info.hugoyu.mytraincontrol.commandstation.task.impl.TurnoutControlTask;
import info.hugoyu.mytraincontrol.switchable.AbstractSwitchable;

public class Turnout extends AbstractSwitchable {

    public Turnout(int address) {
        super(address);
    }

    @Override
    public AbstractCommandStationTask getSwitchControlTask() {
        return new TurnoutControlTask(address, state);
    }
}
