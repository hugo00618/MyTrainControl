package info.hugoyu.mytraincontrol.commandstation;

import com.google.common.annotations.VisibleForTesting;
import info.hugoyu.mytraincontrol.commandstation.task.AbstractCommandStationTask;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

public class CommandStationRunnable implements Runnable {

    private static CommandStationRunnable instance;

    private static final long MIN_UPDATE_INTERVAL = 20;

    @VisibleForTesting
    long nextAvailableExecutionTime = 0;

    @VisibleForTesting
    long nextAvailableHighCurrentExecutionTime = 0;

    private CommandStation commandStation;

    private CommandStationRunnable() {
        commandStation = CommandStation.getInstance();
    }

    @VisibleForTesting
    CommandStationRunnable(CommandStation commandStation) {
        this.commandStation = commandStation;
    }

    public static CommandStationRunnable getInstance() {
        if (instance == null) {
            instance = new CommandStationRunnable();
            new Thread(instance).start();
        }

        return instance;
    }

    @Override
    public void run() {
        while (true) {
            AbstractCommandStationTask task = getNextAvailableToExecuteTask();

            long executionTime = System.currentTimeMillis();

            task.execute();
            task.callback(executionTime);
            
            nextAvailableExecutionTime = executionTime + MIN_UPDATE_INTERVAL;
            nextAvailableHighCurrentExecutionTime = executionTime + Math.max(MIN_UPDATE_INTERVAL,
                    task.getHighCurrentConsumptionPeriod());

            AbstractCommandStationTask nextTask = task.getNextTask(executionTime);
            if (nextTask != null) {
                commandStation.addTask(nextTask);
            }
        }
    }

    @VisibleForTesting
    AbstractCommandStationTask getNextAvailableToExecuteTask() {
        AbstractCommandStationTask task = commandStation.waitForNextTask();
        final long timeToWait = getTimeToWait(task, nextAvailableExecutionTime, nextAvailableHighCurrentExecutionTime);

        if (timeToWait <= 0) {
            return task;
        } else {
            Callable<AbstractCommandStationTask> waitForNewlyAvailableTask = this::getNextAvailableToExecuteTask;

            Callable<AbstractCommandStationTask> waitForCurrentTask = () -> {
                Thread.sleep(timeToWait);
                return task;
            };

            AbstractCommandStationTask firstAvailableTask = null;
            try {
                firstAvailableTask = Executors.newCachedThreadPool().invokeAny(
                        List.of(waitForNewlyAvailableTask, waitForCurrentTask));

            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            } finally {
                if (task != firstAvailableTask) {
                    commandStation.addTask(task);
                }
            }

            return firstAvailableTask;
        }
    }

    private long getTimeToWait(AbstractCommandStationTask task,
                               long nextAvailableExecutionTime,
                               long nextAvailableHighCurrentExecutionTime) {

        if (task.isHighCurrentConsumptionTask()) {
            return Math.max(task.getScheduledExecutionTime(), nextAvailableHighCurrentExecutionTime)
                    - System.currentTimeMillis();
        } else {
            return Math.max(task.getScheduledExecutionTime(), nextAvailableExecutionTime)
                    - System.currentTimeMillis();
        }
    }

}
