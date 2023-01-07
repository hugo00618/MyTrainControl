package info.hugoyu.mytraincontrol.registry.switchable.impl;

import info.hugoyu.mytraincontrol.registry.switchable.AbstractSwitchableRegistry;
import info.hugoyu.mytraincontrol.switchable.Switchable;
import info.hugoyu.mytraincontrol.switchable.impl.Crossover;

import java.util.HashMap;
import java.util.Map;

public class CrossoverRegistry extends AbstractSwitchableRegistry {

    private static CrossoverRegistry instance;

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

    // TODO:
//    public Crossover registerCrossover(CrossoverJson crossoverJson)

    public Crossover getCrossover(int address) {
        if (!crossovers.containsKey(address)) {
            // TODO: add this back when registerCrossover is ready
//            throw new InvalidIdException(address, InvalidIdException.Type.NOT_FOUND);
            crossovers.put(address, new Crossover(address));
        }

        return crossovers.get(address);
    }

    @Override
    public Map<Integer, ? extends Switchable> getSwitchableRegistry() {
        return crossovers;
    }
}
