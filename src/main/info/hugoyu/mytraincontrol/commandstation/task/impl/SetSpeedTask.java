package info.hugoyu.mytraincontrol.commandstation.task.impl;

import com.google.common.annotations.VisibleForTesting;
import info.hugoyu.mytraincontrol.commandstation.task.AbstractCommandStationTask;
import info.hugoyu.mytraincontrol.commandstation.task.Deduplicatable;
import info.hugoyu.mytraincontrol.registry.ThrottleRegistry;
import info.hugoyu.mytraincontrol.trainset.Trainset;
import jmri.DccThrottle;

public class SetSpeedTask extends AbstractCommandStationTask {

    private Trainset trainset;

    public SetSpeedTask(Trainset trainset, long taskCreationTime) {
        super(taskCreationTime);

        this.trainset = trainset;
    }

    public SetSpeedTask(Trainset trainset, long taskCreationTime, long delay) {
        super(taskCreationTime, delay);

        this.trainset = trainset;
    }

    @Override
    public void execute() {
        trainset.prepareForSetSpeedTaskExecution(this);

        DccThrottle throttle = ThrottleRegistry.getInstance().getThrottle(trainset.getAddress());
        throttle.setSpeedSetting(trainset.getThrottle());
    }

    @Override
    public boolean isDuplicate(Deduplicatable task) {
        if (task instanceof SetSpeedTask) {
            return ((SetSpeedTask) task).trainset == trainset;
        }
        return false;
    }

    @Override
    public void dedupe(Deduplicatable task) {
        if (task instanceof SetSpeedTask) {
            scheduledExecutionTime = Math.min(scheduledExecutionTime,
                    ((SetSpeedTask) task).getScheduledExecutionTime());
            isDelayedTask |= ((SetSpeedTask) task).isDelayedTask();
        }
    }

    @VisibleForTesting
    public Trainset getTrainset() {
        return trainset;
    }
}
