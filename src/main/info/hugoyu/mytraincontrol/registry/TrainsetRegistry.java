package info.hugoyu.mytraincontrol.registry;

import info.hugoyu.mytraincontrol.trainset.Trainset;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
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

        trainsets.put(address, new Trainset(address, name, profileFilename));
    }

    public Trainset getTrainset(Integer address) {
        if (address == null) {
            return null;
        }

        return trainsets.get(address);
    }


}
