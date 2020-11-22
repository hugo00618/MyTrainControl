package info.hugoyu.mytraincontrol.trainset;

import info.hugoyu.mytraincontrol.commandstation.SetSpeedTask;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public abstract class AbstractTrainset implements SetSpeedTask.TaskExecution {
    private int address;
    private String name;

    public abstract float getThrottle();
}
