package info.hugoyu.mytraincontrol.util;

import info.hugoyu.mytraincontrol.registries.ThrottleRegistry;
import info.hugoyu.mytraincontrol.registries.TrainsetRegistry;
import info.hugoyu.mytraincontrol.trainset.Trainset;

public class TrainUtil {

    public static void move(int address, int dist) throws Exception {
        Trainset trainset = TrainsetRegistry.getInstance().getTrainset(address);
        trainset.move(dist);
    }

    public static void registerTrainset(int address, String name, String profileFilename) throws Exception {
        TrainsetRegistry.getInstance().registerTrainset(address, name, profileFilename);
        ThrottleRegistry.getInstance().registerThrottle(address);
    }
}
