package info.hugoyu.mytraincontrol.commandstation;

import java.util.PriorityQueue;
import java.util.Queue;

public class CommandStation {

    private static CommandStation instance;

    private static volatile Queue<CommandStationTask> tasks;

    private CommandStation() {
        tasks = new PriorityQueue<>();
    }

    public static CommandStation getInstance() {
        if (instance == null) {
            instance = new CommandStation();
        }
        return instance;
    }

    public void addTask(CommandStationTask task) {
        synchronized (tasks) {
            tasks.add(task);
        }
    }

    public CommandStationTask getAvailableTask() {
        synchronized (tasks) {
            CommandStationTask task = tasks.peek();
            if (task != null && System.currentTimeMillis() >= task.getScheduledExecutionTime()) {
                return tasks.poll();
            } else {
                return null;
            }
        }
    }
}
