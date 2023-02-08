package info.hugoyu.mytraincontrol.util;

import info.hugoyu.mytraincontrol.command.debug.AbstractDebugCommand;
import info.hugoyu.mytraincontrol.trainset.Trainset;

public class AutomaticTrainOperationUtil {

    static {
        // cancel ATO when a debug command is executed
        AbstractDebugCommand.subscribe(new AbstractDebugCommand.EventListener() {
            @Override
            public void onCommandExecuted(AbstractDebugCommand command) {
                TrainUtil.getTrainsets().values().forEach(AutomaticTrainOperationUtil::disableAto);
            }
        });
    }

    public static void enableAto(Trainset trainset) {
        trainset.activateAto();
        System.out.println(String.format("%s: enabling ATO", trainset.getName()));
    }

    public static void disableAto(Trainset trainset) {
        trainset.deactivateAto();
        System.out.println(String.format("%s: disabling ATO", trainset.getName()));
    }
}
