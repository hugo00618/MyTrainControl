package info.hugoyu.mytraincontrol.commandstation;

import info.hugoyu.mytraincontrol.commandstation.task.AbstractCommandStationTask;

import java.util.PriorityQueue;
import java.util.Queue;

public class CommandStation {

    private static CommandStation instance;

    private static volatile Queue<AbstractCommandStationTask> tasks;
    private static final Object tasksLock = new Object();

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
        synchronized (tasksLock) {
            tasks.add(task);
        }
    }

    public AbstractCommandStationTask getAvailableTask() {
        synchronized (tasksLock) {
            AbstractCommandStationTask task = tasks.peek();
            if (task != null && System.currentTimeMillis() >= task.getScheduledExecutionTime()) {
                return tasks.poll();
            } else {
                return null;
            }
        }
    }
}
