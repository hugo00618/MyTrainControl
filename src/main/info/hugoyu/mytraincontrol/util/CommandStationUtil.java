package info.hugoyu.mytraincontrol.util;

import info.hugoyu.mytraincontrol.commandstation.CommandStation;
import info.hugoyu.mytraincontrol.commandstation.task.AbstractCommandStationTask;

public class CommandStationUtil {

    public static void addTask(AbstractCommandStationTask task) {
        CommandStation.getInstance().addTask(task);
    }

}
