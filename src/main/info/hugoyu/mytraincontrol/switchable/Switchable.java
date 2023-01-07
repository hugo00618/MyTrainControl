package info.hugoyu.mytraincontrol.switchable;

import info.hugoyu.mytraincontrol.commandstation.task.AbstractCommandStationTask;

public interface Switchable {

    enum State {
        THROWN(jmri.Turnout.THROWN),
        CLOSED(jmri.Turnout.CLOSED),
        UNKNOWN(-1);

        public final int stateCode;

        State(int stateCode) {
            this.stateCode = stateCode;
        }
    }

    void setState(State state);
    State getState();
    AbstractCommandStationTask getSwitchControlTask();

}
