package info.hugoyu.mytraincontrol.commandstation;

import com.google.common.annotations.VisibleForTesting;
import info.hugoyu.mytraincontrol.commandstation.task.AbstractCommandStationTask;

import java.util.Optional;
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

    @VisibleForTesting
    static CommandStation getNewInstance() {
        instance = new CommandStation();
        return instance;
    }

    public void addTask(AbstractCommandStationTask newTask) {
        synchronized (tasksLock) {
            Optional<AbstractCommandStationTask> duplicateTaskOptional = tasks.stream()
                    .filter(task -> task.isDuplicate(newTask))
                    .findFirst();

            if (duplicateTaskOptional.isPresent()) {
                AbstractCommandStationTask duplicateTask = duplicateTaskOptional.get();
                removeFromTasks(duplicateTask);
                duplicateTask.dedupe(newTask);
                tasks.add(duplicateTask);
            } else {
                tasks.add(newTask);
            }

            tasksLock.notifyAll();
        }
    }

    public AbstractCommandStationTask getNextAvailableTask(boolean includeHighConsumptionTasks) {
        synchronized (tasksLock) {
            return tasks.stream()
                    .filter(task -> includeHighConsumptionTasks || !task.isHighCurrentConsumptionTask())
                    .findFirst()
                    .orElseGet(() -> {
                        try {
                            tasksLock.wait();
                        } catch (InterruptedException e) {

                        }
                        return getNextAvailableTask(includeHighConsumptionTasks);
                    });
        }
    }

    public void removeFromTasks(AbstractCommandStationTask removingTask) {
        synchronized (tasksLock) {
            if (removingTask != null) {
                Queue<AbstractCommandStationTask> newTasks = new PriorityQueue<>();
                while (!tasks.isEmpty()) {
                    AbstractCommandStationTask task = tasks.poll();
                    if (task != removingTask) {
                        newTasks.add(task);
                    }
                }
                tasks = newTasks;
            }
        }
    }

    @VisibleForTesting
    Queue<AbstractCommandStationTask> getTasks() {
        return tasks;
    }
}
