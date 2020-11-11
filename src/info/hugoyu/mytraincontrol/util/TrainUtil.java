package info.hugoyu.mytraincontrol.util;

import info.hugoyu.mytraincontrol.registries.ThrottleRegistry;
import info.hugoyu.mytraincontrol.registries.TrainsetRegistry;
import info.hugoyu.mytraincontrol.trainset.Trainset;

public class TrainUtil {

    public static void setSpeed(int address, double speed) throws Exception {
        Trainset trainset = TrainsetRegistry.getInstance().getTrainset(address);
        trainset.setSpeed(speed);
    }

    public static void registerTrainset(int address, String name, String profileFilename) throws Exception {
        TrainsetRegistry.getInstance().registerTrainset(address, name, profileFilename);
        ThrottleRegistry.getInstance().registerThrottle(address);
    }
}
