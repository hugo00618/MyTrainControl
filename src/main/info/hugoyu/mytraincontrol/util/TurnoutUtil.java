package info.hugoyu.mytraincontrol.util;

import info.hugoyu.mytraincontrol.registry.TurnoutRegistry;
import info.hugoyu.mytraincontrol.turnout.Turnout;

public class TurnoutUtil {

    /**
     * changes the turnout state and sends turnout control task if the state is changed
     * @param turnout
     * @param state
     * @param forceSend ignores cached turnout state and forces sending the turnout control task
     */
    public static void setTurnoutState(Turnout turnout, Turnout.State state, boolean forceSend) {
        TurnoutRegistry turnoutRegistry = TurnoutRegistry.getInstance();
        boolean isTurnoutStateChanged = turnoutRegistry.setTurnoutState(turnout, state);
        if (forceSend || isTurnoutStateChanged) {
            CommandStationUtil.addTask(turnout.getTurnoutControlTask());
        }
    }
}
