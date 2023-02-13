package info.hugoyu.mytraincontrol.commandstation;

import info.hugoyu.mytraincontrol.commandstation.task.AbstractCommandStationTask;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CommandStationRunnable implements Runnable {

    private static CommandStationRunnable instance;

    private static final long MIN_UPDATE_INTERVAL = 20;

    private long nextAvailableExecutionTime = 0;
    private long nextAvailableHighCurrentExecutionTime = 0;
    private CommandStation commandStation;

    private CommandStationRunnable() {
        commandStation = CommandStation.getInstance();
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
            Callable<AbstractCommandStationTask> nextTask = () -> {
                AbstractCommandStationTask task = commandStation.getNextAvailableTask(false);
                sleep(task);
                return task;
            };

            Callable<AbstractCommandStationTask> nextHighConsumptionTask = () -> {
                AbstractCommandStationTask task = commandStation.getNextAvailableTask(true);
                sleep(task);
                return task;
            };

            ExecutorService executorService = Executors.newFixedThreadPool(2);
            AbstractCommandStationTask task;
            try {
                task = executorService.invokeAny(List.of(nextTask, nextHighConsumptionTask));
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            } finally {
                executorService.shutdownNow();
            }
            commandStation.removeFromTasks(task);

            long executionTime = System.currentTimeMillis();

            task.execute();
            task.callback(executionTime);


            nextAvailableExecutionTime = executionTime + MIN_UPDATE_INTERVAL;
            nextAvailableHighCurrentExecutionTime = executionTime + task.getHighCurrentConsumptionPeriod();

            Optional.ofNullable(task.getNextTask(executionTime))
                    .ifPresent(followupTask -> commandStation.addTask(followupTask));

        }
    }

    private void sleep(AbstractCommandStationTask task) {
        long until = Math.max(task.getScheduledExecutionTime(),
                task.isHighCurrentConsumptionTask() ? nextAvailableHighCurrentExecutionTime : nextAvailableExecutionTime);
        sleep(until);
    }

    private void sleep(long until) {
        try {
            // sleep till next execution window opens
            long sleepTime = until - System.currentTimeMillis();
            if (sleepTime > 0) {
                Thread.sleep(sleepTime);
            }
        } catch (InterruptedException e) {

        }
    }
}
