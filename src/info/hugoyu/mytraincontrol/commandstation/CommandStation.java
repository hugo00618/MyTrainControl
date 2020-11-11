package info.hugoyu.mytraincontrol.commandstation;

import java.util.LinkedList;
import java.util.Queue;

public class CommandStation {

    private static CommandStation instance;

    private static Queue<CommandStationTask> tasks;

    private CommandStation() {
        tasks = new LinkedList<>();
    }

    public static CommandStation getInstance() {
        if (instance == null) {
            instance = new CommandStation();
        }
        return instance;
    }

    public boolean hasTasks() {
        synchronized(tasks) {
            return !tasks.isEmpty();
        }
    }

    public void addTask(CommandStationTask task) {
        synchronized (tasks) {
            tasks.add(task);
        }
    }

    public CommandStationTask getTask() {
        synchronized (tasks) {
            return tasks.poll();
        }
    }
}
