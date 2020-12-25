package info.hugoyu.mytraincontrol.commandstation;

import lombok.Getter;

@Getter
public abstract class AbstractCommandStationTask implements Comparable<AbstractCommandStationTask> {
    private long taskCreationTime;
    private long scheduledExecutionTime;
    private boolean isDelayedTask;

    public AbstractCommandStationTask(long taskCreationTime) {
        this.taskCreationTime = taskCreationTime;
        scheduledExecutionTime = taskCreationTime;
    }

    public AbstractCommandStationTask(long taskCreationTime, long delay) {
        this(taskCreationTime);
        scheduledExecutionTime += delay;
        isDelayedTask = true;
    }

    abstract void execute();

    @Override
    public int compareTo(AbstractCommandStationTask o) {
        return (int) (scheduledExecutionTime - o.scheduledExecutionTime);
    }
}
