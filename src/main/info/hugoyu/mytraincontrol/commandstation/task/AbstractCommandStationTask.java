package info.hugoyu.mytraincontrol.commandstation.task;

import javafx.util.Pair;
import lombok.Getter;
import lombok.Setter;

public abstract class AbstractCommandStationTask implements Comparable<AbstractCommandStationTask>, Deduplicatable {

    @Getter
    private final long taskCreationTime;

    @Setter
    @Getter
    protected long scheduledExecutionTime;

    @Getter
    protected boolean isDelayedTask;

    /**
     * Defines whether the task consumes high current and how long does it last for.
     * Defaults to 0 if the task does not consume high current.
     */
    @Getter
    protected long highCurrentConsumptionPeriod;

    // nextTask, delay
    protected Pair<AbstractCommandStationTask, Long> nextTask;

    protected AbstractCommandStationTask() {
        this(System.currentTimeMillis());
    }

    protected AbstractCommandStationTask(long taskCreationTime, long delay) {
        this(taskCreationTime);
        addDelay(delay);
    }

    protected AbstractCommandStationTask(long taskCreationTime) {
        this.taskCreationTime = taskCreationTime;
        this.scheduledExecutionTime = taskCreationTime;
    }

    private void addDelay(long delay) {
        scheduledExecutionTime += delay;
        isDelayedTask = true;
    }

    public abstract void execute();

    public AbstractCommandStationTask getNextTask(long actualExecutionTime) {
        if (nextTask == null) {
            return null;
        }
        AbstractCommandStationTask task = nextTask.getKey();
        long delay = nextTask.getValue();

        task.setScheduledExecutionTime(actualExecutionTime);
        task.addDelay(delay);

        return task;
    }

    public boolean isHighCurrentConsumptionTask() {
        return highCurrentConsumptionPeriod != 0;
    }

    public boolean isDuplicate(Deduplicatable task) {
        return false;
    }

    public void dedupe(Deduplicatable task) {

    }

    @Override
    public int compareTo(AbstractCommandStationTask o) {
        return (int) (scheduledExecutionTime - o.scheduledExecutionTime);
    }
}
