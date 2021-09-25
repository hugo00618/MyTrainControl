package info.hugoyu.mytraincontrol.commandstation.task;

import info.hugoyu.mytraincontrol.trainset.Trainset;
import lombok.Getter;

@Getter
public abstract class AbstractCommandStationTask implements Comparable<AbstractCommandStationTask> {
    protected Trainset trainset;
    private long taskCreationTime;
    private long scheduledExecutionTime;
    private boolean isDelayedTask;

    public AbstractCommandStationTask(Trainset trainset, long taskCreationTime) {
        this.trainset = trainset;
        this.taskCreationTime = taskCreationTime;
        scheduledExecutionTime = taskCreationTime;
    }

    public AbstractCommandStationTask(Trainset trainset, long taskCreationTime, long delay) {
        this(trainset, taskCreationTime);
        scheduledExecutionTime += delay;
        isDelayedTask = true;
    }

    public void dedupe(AbstractCommandStationTask task) {
        scheduledExecutionTime = Math.min(scheduledExecutionTime, task.scheduledExecutionTime);
        isDelayedTask |= task.isDelayedTask;
    }

    public abstract void execute();

    @Override
    public int compareTo(AbstractCommandStationTask o) {
        return (int) (scheduledExecutionTime - o.scheduledExecutionTime);
    }
}
