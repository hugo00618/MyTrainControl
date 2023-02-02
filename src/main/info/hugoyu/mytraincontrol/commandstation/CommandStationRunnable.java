package info.hugoyu.mytraincontrol.commandstation;

import info.hugoyu.mytraincontrol.commandstation.task.AbstractCommandStationTask;

import java.util.Optional;

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
            sleepIfNeeded();

            AbstractCommandStationTask task = commandStation.getAvailableTask(true);
            if (task != null) {
                task.execute();

                long executionTime = System.currentTimeMillis();
                nextAvailableExecutionTime = executionTime + MIN_UPDATE_INTERVAL;
                nextAvailableHighCurrentExecutionTime = executionTime + task.getHighCurrentConsumptionPeriod();

                AbstractCommandStationTask nextTask = task.getNextTask(executionTime);
                if (nextTask != null) {
                    commandStation.addTask(nextTask);
                }
            }
        }
    }

    private void sleepIfNeeded() {
        long nextAvailableTime = nextAvailableExecutionTime;
        if (isNextTaskHighCurrentConsumption()) {
            nextAvailableTime = Math.max(nextAvailableTime, nextAvailableHighCurrentExecutionTime);
        }
        sleep(nextAvailableTime);
    }

    private boolean isNextTaskHighCurrentConsumption() {
        return Optional.ofNullable(commandStation.getAvailableTask(false))
                .map(AbstractCommandStationTask::isHighCurrentConsumptionTask)
                .orElse(false);
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
