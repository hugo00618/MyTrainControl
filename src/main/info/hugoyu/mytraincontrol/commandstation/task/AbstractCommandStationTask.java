package info.hugoyu.mytraincontrol.commandstation.task;

import lombok.Getter;

import java.util.List;

@Getter
public abstract class AbstractCommandStationTask implements Comparable<AbstractCommandStationTask>, Deduplicatable {
    private long taskCreationTime;
    protected long scheduledExecutionTime;
    protected boolean isDelayedTask;

    private List<AbstractCommandStationTask> subtasks;

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

    /**
     * Recursive definition of tasks
     * @param subtasks
     */
    protected AbstractCommandStationTask(List<AbstractCommandStationTask> subtasks) {
        this.subtasks = subtasks;
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
