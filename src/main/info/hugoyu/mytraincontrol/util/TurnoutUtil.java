package info.hugoyu.mytraincontrol.util;

import info.hugoyu.mytraincontrol.commandstation.task.impl.TurnoutControlTask;
import jmri.Turnout;

public class TurnoutUtil {

    public enum TurnoutState {
        THROWN(Turnout.THROWN),
        CLOSED(Turnout.CLOSED);

        public int stateCode;

        TurnoutState(int stateCode) {
            this.stateCode = stateCode;
        }
    }

    public static void setTurnoutState(String address, TurnoutState state) {
        CommandStationUtil.addTask(new TurnoutControlTask(address, state));
    }
}
