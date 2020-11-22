package info.hugoyu.mytraincontrol.commandstation;

import info.hugoyu.mytraincontrol.registries.ThrottleRegistry;
import info.hugoyu.mytraincontrol.trainset.AbstractTrainset;
import jmri.DccThrottle;

public class SetSpeedTask extends CommandStationTask {

    public interface TaskExecution {
        void prepareForSetSpeedTaskExecution(CommandStationTask task);
    }

    private AbstractTrainset trainset;

    public SetSpeedTask(AbstractTrainset trainset) {
        super();
        this.trainset = trainset;
    }

    public SetSpeedTask(AbstractTrainset trainset, long delay) {
        super(delay);
        this.trainset = trainset;
    }

    @Override
    public void execute() {
        trainset.prepareForSetSpeedTaskExecution(this);

        DccThrottle throttle = ThrottleRegistry.getInstance().getThrottle(trainset.getAddress());
        throttle.setSpeedSetting(trainset.getThrottle());
    }

}
