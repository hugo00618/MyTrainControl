package info.hugoyu.mytraincontrol.util;

import info.hugoyu.mytraincontrol.registry.TurnoutRegistry;
import info.hugoyu.mytraincontrol.turnout.Turnout;

public class TurnoutUtil {

    /**
     * changes the turnout state and sends turnout control task if the state is changed
     * @param address
     * @param state
     * @param forceSend ignores cached turnout state and forces sending the turnout control task
     */
    public static void setTurnoutState(int address, Turnout.State state, boolean forceSend) {
        TurnoutRegistry turnoutRegistry = TurnoutRegistry.getInstance();
        boolean isTurnoutStateChanged = turnoutRegistry.setTurnoutState(address, state);
        if (forceSend || isTurnoutStateChanged) {
            Turnout turnout = turnoutRegistry.getTurnout(address);
            CommandStationUtil.addTask(turnout.getTurnoutControlTask());
        }
    }
}
