package info.hugoyu.mytraincontrol.util;

import info.hugoyu.mytraincontrol.commandstation.task.AbstractCommandStationTask;
import info.hugoyu.mytraincontrol.registry.SwitchableRegistry;
import info.hugoyu.mytraincontrol.switchable.AbstractSwitchable;

import java.util.function.Consumer;

public class SwitchUtil {

    /**
     * changes the turnout state and sends turnout control task if the state is changed
     * @param switchable
     * @param state
     * @param forceSend ignores cached turnout state and forces sending the turnout control task
     * @param callback callback function
     */
    public static void setSwitchState(AbstractSwitchable switchable,
                                      AbstractSwitchable.State state,
                                      boolean forceSend,
                                      Consumer<Long> callback) {
        AbstractSwitchable.State cachedState = switchable.getState();
        boolean isSwitchStateChanged = cachedState != state;
        switchable.setState(state);

        if (forceSend || isSwitchStateChanged) {
            AbstractCommandStationTask switchControlTask = switchable.getSwitchControlTask();
            if (callback != null) {
                switchControlTask.addCallbackFunction(callback);
            }
            CommandStationUtil.addTask(switchControlTask);
        } else {
            if (callback != null) {
                callback.accept(-1L);
            }
        }
    }

    public static void setSwitchState(AbstractSwitchable switchable, AbstractSwitchable.State state, Consumer<Long> callback) {
        setSwitchState(switchable, state, false, callback);
    }

    public static void invalidateCachedState() {
        SwitchableRegistry.getInstance().getSwitchables().values()
                .forEach(switchable -> switchable.setState(AbstractSwitchable.State.UNKNOWN));
    }

}
