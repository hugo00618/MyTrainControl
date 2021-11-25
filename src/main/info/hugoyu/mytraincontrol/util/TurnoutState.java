package info.hugoyu.mytraincontrol.util;

import jmri.Turnout;

public enum TurnoutState {
    THROWN(Turnout.THROWN),
    CLOSED(Turnout.CLOSED),
    UNKNOWN(-1);

    public int stateCode;

    TurnoutState(int stateCode) {
        this.stateCode = stateCode;
    }
}