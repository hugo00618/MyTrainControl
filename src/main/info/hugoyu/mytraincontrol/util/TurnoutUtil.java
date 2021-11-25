package info.hugoyu.mytraincontrol.util;

import info.hugoyu.mytraincontrol.commandstation.task.impl.TurnoutControlTask;
import info.hugoyu.mytraincontrol.registry.TurnoutRegistry;

public class TurnoutUtil {

    /**
     * changes the turnout state and sends turnout control task if the state is changed
     * @param address
     * @param state
     * @param forceControl force sending the turnout control task
     */
    public static void setTurnoutState(int address, TurnoutState state, boolean forceControl) {
        boolean isTurnoutStateChanged = TurnoutRegistry.getInstance().setTurnoutState(address, state);
        if (forceControl || isTurnoutStateChanged) {
            CommandStationUtil.addTask(new TurnoutControlTask(address, state));
        }
    }
}
