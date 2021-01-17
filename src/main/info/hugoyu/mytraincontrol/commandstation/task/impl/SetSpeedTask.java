package info.hugoyu.mytraincontrol.commandstation.task.impl;

import info.hugoyu.mytraincontrol.commandstation.task.AbstractCommandStationTask;
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

}
