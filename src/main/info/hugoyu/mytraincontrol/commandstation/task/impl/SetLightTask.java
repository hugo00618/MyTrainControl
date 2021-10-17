package info.hugoyu.mytraincontrol.commandstation.task.impl;

import info.hugoyu.mytraincontrol.commandstation.task.AbstractTrainsetTask;
import info.hugoyu.mytraincontrol.registry.ThrottleRegistry;
import info.hugoyu.mytraincontrol.trainset.Trainset;
import jmri.DccThrottle;

public class SetLightTask extends AbstractTrainsetTask {

    public SetLightTask(Trainset trainset) {
        super(trainset);
    }

    @Override
    public void execute() {
        DccThrottle throttle = ThrottleRegistry.getInstance().getThrottle(trainset.getAddress());
        throttle.setF0(trainset.isLightOn());
    }

}
