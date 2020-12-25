package info.hugoyu.mytraincontrol.registries;

import info.hugoyu.mytraincontrol.trainset.Trainset;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

public class TrainsetRegistry {

    private static TrainsetRegistry instance;

    private Map<Integer, Trainset> trainsets;

    private TrainsetRegistry() {
        trainsets = new HashMap<>();
    }

    public static TrainsetRegistry getInstance() {
        if (instance == null) {
            instance = new TrainsetRegistry();
        }

        return instance;
    }

    public void registerTrainset(int address, String name, String profileFilename) throws Exception {
        if (trainsets.containsKey(address)) {
            throw new Exception(String.format("ERROR: address %d already registered", address));
        }

        try {
            trainsets.put(address, new Trainset(address, name, profileFilename));
        } catch (FileNotFoundException e) {
            throw new Exception("ERROR: File not found: " + profileFilename);
        }
    }

    public Trainset getTrainset(int address) throws Exception {
        Trainset trainset = trainsets.get(address);

        if (trainset == null) {
            throw new Exception(String.format("ERROR: address %d does not exist", address));
        }

        return trainset;
    }


}
