package info.hugoyu.mytraincontrol.switchable.impl;

import info.hugoyu.mytraincontrol.commandstation.task.AbstractCommandStationTask;
import info.hugoyu.mytraincontrol.commandstation.task.impl.TurnoutControlTask;
import info.hugoyu.mytraincontrol.switchable.Switchable;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Turnout implements Switchable {

    private int address;
    private State state;

    public Turnout(int address) {
        this.address = address;
        this.state = State.UNKNOWN;
    }

    @Override
    public AbstractCommandStationTask getSwitchControlTask() {
        return new TurnoutControlTask(address, state);
    }
}
