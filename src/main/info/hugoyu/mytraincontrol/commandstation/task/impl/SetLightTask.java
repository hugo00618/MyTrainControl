package info.hugoyu.mytraincontrol.commandstation.task.impl;

import info.hugoyu.mytraincontrol.commandstation.task.AbstractCommandStationTask;
import info.hugoyu.mytraincontrol.registry.ThrottleRegistry;
import info.hugoyu.mytraincontrol.trainset.Trainset;
import info.hugoyu.mytraincontrol.util.LightState;
import jmri.DccThrottle;

public class SetLightTask extends AbstractCommandStationTask {

    private Trainset trainset;

    public SetLightTask(Trainset trainset) {
        super();

        this.trainset = trainset;
    }

    @Override
    public void execute() {
        DccThrottle throttle = ThrottleRegistry.getInstance().getThrottle(trainset.getAddress());
        throttle.setF0(trainset.getLightState() == LightState.ON);
    }

}
