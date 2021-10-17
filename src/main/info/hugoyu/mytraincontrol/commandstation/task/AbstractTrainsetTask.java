package info.hugoyu.mytraincontrol.commandstation.task;

import info.hugoyu.mytraincontrol.trainset.Trainset;
import lombok.Getter;

public abstract class AbstractTrainsetTask extends AbstractCommandStationTask {

    @Getter
    protected Trainset trainset;

    public AbstractTrainsetTask(Trainset trainset) {
        super();

        this.trainset = trainset;
    }

    public AbstractTrainsetTask(Trainset trainset, long taskCreationTime) {
        super(taskCreationTime);

        this.trainset = trainset;
    }

    public AbstractTrainsetTask(Trainset trainset, long taskCreationTime, long delay) {
        super(taskCreationTime, delay);

        this.trainset = trainset;
    }
}
