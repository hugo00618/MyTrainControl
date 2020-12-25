package info.hugoyu.mytraincontrol.commandstation;

import java.util.PriorityQueue;
import java.util.Queue;

public class CommandStation {

    private static CommandStation instance;

    private static volatile Queue<AbstractCommandStationTask> tasks;

    private CommandStation() {
        tasks = new PriorityQueue<>();
    }

    public static CommandStation getInstance() {
        if (instance == null) {
            instance = new CommandStation();
        }
        return instance;
    }

    public void addTask(AbstractCommandStationTask task) {
        synchronized (tasks) {
            tasks.add(task);
        }
    }

    public AbstractCommandStationTask getAvailableTask() {
        synchronized (tasks) {
            AbstractCommandStationTask task = tasks.peek();
            if (task != null && System.currentTimeMillis() >= task.getScheduledExecutionTime()) {
                return tasks.poll();
            } else {
                return null;
            }
        }
    }
}
