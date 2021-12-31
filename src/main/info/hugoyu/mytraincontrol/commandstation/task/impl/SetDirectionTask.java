package info.hugoyu.mytraincontrol.commandstation.task.impl;

import info.hugoyu.mytraincontrol.commandstation.task.AbstractCommandStationTask;
import info.hugoyu.mytraincontrol.registry.ThrottleRegistry;
import info.hugoyu.mytraincontrol.trainset.Trainset;
import jmri.DccThrottle;

public class SetDirectionTask extends AbstractCommandStationTask {

    private Trainset trainset;

    public SetDirectionTask(Trainset trainset) {
        super();

        this.trainset = trainset;
    }

    @Override
    public void execute() {
        DccThrottle throttle = ThrottleRegistry.getInstance().getThrottle(trainset.getAddress());
        throttle.setIsForward(trainset.isForward());
    }

}
