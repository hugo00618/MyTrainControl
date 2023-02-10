package info.hugoyu.mytraincontrol.switchable;

import info.hugoyu.mytraincontrol.commandstation.task.AbstractCommandStationTask;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
public abstract class AbstractSwitchable {

    public enum State {
        THROWN(jmri.Turnout.THROWN),
        CLOSED(jmri.Turnout.CLOSED),
        UNKNOWN(-1);

        public final int stateCode;

        State(int stateCode) {
            this.stateCode = stateCode;
        }
    }

    public enum Type {
        TURNOUT,
        CROSSOVER,
    }

    protected final int address;

    @Setter
    @Getter
    protected State state = State.UNKNOWN;

    public abstract AbstractCommandStationTask getSwitchControlTask();

}
