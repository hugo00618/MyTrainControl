package info.hugoyu.mytraincontrol.commandstation.task;

import lombok.Getter;

@Getter
public abstract class AbstractCommandStationTask implements Comparable<AbstractCommandStationTask> {
    private long taskCreationTime;
    private long scheduledExecutionTime;
    private boolean isDelayedTask;

    public AbstractCommandStationTask() {
        this(System.currentTimeMillis());
    }

    public AbstractCommandStationTask(long taskCreationTime) {
        this.taskCreationTime = taskCreationTime;
        this.scheduledExecutionTime = taskCreationTime;
    }
    
    public AbstractCommandStationTask(long taskCreationTime, long delay) {
        this(taskCreationTime);
        this.scheduledExecutionTime += delay;
        this.isDelayedTask = true;
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
