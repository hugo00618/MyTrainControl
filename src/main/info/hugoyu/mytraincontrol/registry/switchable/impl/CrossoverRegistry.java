package info.hugoyu.mytraincontrol.registry.switchable.impl;

import info.hugoyu.mytraincontrol.exception.InvalidIdException;
import info.hugoyu.mytraincontrol.json.layout.CrossoverJson;
import info.hugoyu.mytraincontrol.registry.switchable.AbstractSwitchableRegistry;
import info.hugoyu.mytraincontrol.switchable.Switchable;
import info.hugoyu.mytraincontrol.switchable.impl.Crossover;

import java.util.HashMap;
import java.util.Map;

public class CrossoverRegistry extends AbstractSwitchableRegistry {

    private static CrossoverRegistry instance;

    // <decoder address, Crossover>
    private Map<Integer, Crossover> crossovers;

    private CrossoverRegistry() {
        crossovers = new HashMap<>();
    }

    public static CrossoverRegistry getInstance() {
        if (instance == null) {
            instance = new CrossoverRegistry();
        }
        return instance;
    }

    public Crossover registerCrossover(CrossoverJson crossoverJson) {
        final int address = crossoverJson.getAddress();
        Crossover crossover = new Crossover(address);
        crossovers.put(address, crossover);
        return crossover;
    }

    public Crossover getCrossover(int address) {
        if (!crossovers.containsKey(address)) {
            throw new InvalidIdException(address, InvalidIdException.Type.NOT_FOUND);
        }

        return crossovers.get(address);
    }

    @Override
    public Map<Integer, ? extends Switchable> getSwitchableRegistry() {
        return crossovers;
    }
}
