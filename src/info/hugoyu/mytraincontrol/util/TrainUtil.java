package info.hugoyu.mytraincontrol.util;

import info.hugoyu.mytraincontrol.DataProvider;
import info.hugoyu.mytraincontrol.Trainset;

public class TrainUtil {

    public static void setSpeed(int address, float speed) throws Exception {
        Trainset trainset = DataProvider.getInstance().getTrainset(address);
        trainset.setSpeed(speed);
    }

    public static void registerTrainset(int address, String name) throws Exception {
        DataProvider.getInstance().registerTrainset(address, name);
    }
}
