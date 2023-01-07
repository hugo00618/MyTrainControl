package info.hugoyu.mytraincontrol.util;

import info.hugoyu.mytraincontrol.commandstation.task.impl.SetThrottleTask;
import info.hugoyu.mytraincontrol.turnout.Turnout;
import jmri.DccThrottle;

import static info.hugoyu.mytraincontrol.turnout.Turnout.State.CLOSED;

public class CrossoverUtil {

    public static void setTurnoutState(DccThrottle crossover, Turnout.State state, boolean forceSend) {
        int throttle = state == CLOSED ? 100 : -100;
        CommandStationUtil.addTask(new SetThrottleTask(crossover, throttle));
        CommandStationUtil.addTask(new SetThrottleTask(crossover, 0, 100));
    }
}
