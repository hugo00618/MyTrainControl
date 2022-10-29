package info.hugoyu.mytraincontrol.registry;

import info.hugoyu.mytraincontrol.exception.InvalidIdException;
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

    public void registerTrainset(int address, Trainset trainset) {
        if (trainsets.containsKey(address)) {
            throw new InvalidIdException(address, InvalidIdException.Type.DUPLICATE);
        }
        trainsets.put(address, trainset);
    }

    public Trainset getTrainset(int address) {
        if (!trainsets.containsKey(address)) {
            throw new InvalidIdException(address, InvalidIdException.Type.NOT_FOUND);
        }
        return trainsets.get(address);
    }


}
