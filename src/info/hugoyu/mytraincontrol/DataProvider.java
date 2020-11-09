package info.hugoyu.mytraincontrol;

import java.util.HashMap;
import java.util.Map;

public class DataProvider {

    private static DataProvider instance;

    private Map<Integer, Trainset> trainsets;

    private DataProvider() {
        trainsets = new HashMap<>();
    }

    public static DataProvider getInstance() {
        if (instance == null) {
            instance = new DataProvider();
        }

        return instance;
    }

    public void registerTrainset(int address, String name) throws Exception {
        if (trainsets.containsKey(address)) {
            throw new Exception(String.format("ERROR: address %d already registered", address));
        }

        trainsets.put(address, new Trainset(address, name));
    }

    public Trainset getTrainset(int address) throws Exception {
        Trainset trainset = trainsets.get(address);

        if (trainset == null) {
            throw new Exception(String.format("ERROR: address %d does not exist", address));
        }

        return trainset;
    }


}
