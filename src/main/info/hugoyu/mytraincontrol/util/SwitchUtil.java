package info.hugoyu.mytraincontrol.util;

import info.hugoyu.mytraincontrol.commandstation.task.AbstractCommandStationTask;
import info.hugoyu.mytraincontrol.switchable.Switchable;

import java.util.function.Consumer;

public class SwitchUtil {

    /**
     * changes the turnout state and sends turnout control task if the state is changed
     * @param switchable
     * @param state
     * @param forceSend ignores cached turnout state and forces sending the turnout control task
     * @param callback callback function
     */
    public static void setSwitchState(Switchable switchable,
                                      Switchable.State state,
                                      boolean forceSend,
                                      Consumer<Long> callback) {
        Switchable.State cachedState = switchable.getState();
        boolean isSwitchStateChanged = cachedState != state;
        switchable.setState(state);

        if (forceSend || isSwitchStateChanged) {
            AbstractCommandStationTask switchControlTask = switchable.getSwitchControlTask();
            if (callback != null) {
                switchControlTask.addCallbackFunction(callback);
            }
            CommandStationUtil.addTask(switchControlTask);
        }
    }

    public static void setSwitchState(Switchable switchable, Switchable.State state, Consumer<Long> callback) {
        setSwitchState(switchable, state, false, callback);
    }

}
