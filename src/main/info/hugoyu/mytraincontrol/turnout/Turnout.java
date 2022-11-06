package info.hugoyu.mytraincontrol.turnout;

import info.hugoyu.mytraincontrol.commandstation.task.AbstractCommandStationTask;
import info.hugoyu.mytraincontrol.commandstation.task.impl.TurnoutControlTask;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Turnout {

    public enum State {
        THROWN(jmri.Turnout.THROWN),
        CLOSED(jmri.Turnout.CLOSED),
        UNKNOWN(-1);

        public final int stateCode;

        State(int stateCode) {
            this.stateCode = stateCode;
        }
    }

    private int address;
    private State state;

    public Turnout(int address) {
        this.address = address;
        this.state = State.UNKNOWN;
    }

    public AbstractCommandStationTask getTurnoutControlTask() {
        return new TurnoutControlTask(address, state);
    }
}
