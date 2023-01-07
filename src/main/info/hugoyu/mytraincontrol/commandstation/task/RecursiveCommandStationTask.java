package info.hugoyu.mytraincontrol.commandstation.task;

import java.util.List;

public final class RecursiveCommandStationTask extends AbstractCommandStationTask {

    public RecursiveCommandStationTask(List<AbstractCommandStationTask> tasks) {
        super(tasks);
    }

    @Override
    public void execute() {

    }
}
