package info.hugoyu.mytraincontrol.commandstation.task.impl;

import info.hugoyu.mytraincontrol.commandstation.task.AbstractTrainsetTask;
import info.hugoyu.mytraincontrol.registry.ThrottleRegistry;
import info.hugoyu.mytraincontrol.trainset.Trainset;
import jmri.DccThrottle;

public class SetSpeedTask extends AbstractTrainsetTask {

    public SetSpeedTask(Trainset trainset, long taskCreationTime) {
        super(trainset, taskCreationTime);
    }

    public SetSpeedTask(Trainset trainset, long taskCreationTime, long delay) {
        super(trainset, taskCreationTime, delay);
    }

    @Override
    public void execute() {
        trainset.prepareForSetSpeedTaskExecution(this);

        DccThrottle throttle = ThrottleRegistry.getInstance().getThrottle(trainset.getAddress());
        throttle.setSpeedSetting(trainset.getThrottle());
    }

}
