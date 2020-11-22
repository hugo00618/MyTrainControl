package info.hugoyu.mytraincontrol.commandstation;

import lombok.Getter;

@Getter
public abstract class CommandStationTask implements Comparable<CommandStationTask> {
    private long taskCreationTime;
    private long scheduledExecutionTime;
    private boolean isDelayedTask;

    public CommandStationTask() {
        taskCreationTime = System.currentTimeMillis();
        scheduledExecutionTime = taskCreationTime;
    }

    public CommandStationTask(long delay) {
        this();
        scheduledExecutionTime += delay;
        isDelayedTask = true;
    }

    abstract void execute();

    @Override
    public int compareTo(CommandStationTask o) {
        return (int) (scheduledExecutionTime - o.scheduledExecutionTime);
    }
}
