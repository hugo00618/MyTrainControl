package info.hugoyu.mytraincontrol.commandstation.task;

import lombok.Getter;

@Getter
public abstract class AbstractCommandStationTask implements Comparable<AbstractCommandStationTask>, Deduplicatable {
    private long taskCreationTime;
    protected long scheduledExecutionTime;
    protected boolean isDelayedTask;

    protected AbstractCommandStationTask() {
        this(System.currentTimeMillis());
    }

    protected AbstractCommandStationTask(long taskCreationTime, long delay) {
        this(taskCreationTime);
        this.scheduledExecutionTime += delay;
        this.isDelayedTask = true;
    }

    protected AbstractCommandStationTask(long taskCreationTime) {
        this.taskCreationTime = taskCreationTime;
        this.scheduledExecutionTime = taskCreationTime;
    }

    public abstract void execute();

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
