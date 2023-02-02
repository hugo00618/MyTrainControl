package info.hugoyu.mytraincontrol.util;

import info.hugoyu.mytraincontrol.switchable.Switchable;

public class SwitchUtil {

    /**
     * changes the turnout state and sends turnout control task if the state is changed
     * @param switchable
     * @param state
     * @param forceSend ignores cached turnout state and forces sending the turnout control task
     */
    public static void setSwitchState(Switchable switchable, Switchable.State state, boolean forceSend) {
        Switchable.State cachedState = switchable.getState();
        boolean isSwitchStateChanged = cachedState != state;
        switchable.setState(state);

        if (forceSend || isSwitchStateChanged) {
            CommandStationUtil.addTask(switchable.getSwitchControlTask());
        }
    }

    public static void setSwitchState(Switchable switchable, Switchable.State state) {
        setSwitchState(switchable, state, false);
    }

}
