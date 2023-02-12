package info.hugoyu.mytraincontrol.registry;

import info.hugoyu.mytraincontrol.exception.InvalidIdException;
import info.hugoyu.mytraincontrol.switchable.AbstractSwitchable;
import info.hugoyu.mytraincontrol.switchable.impl.Crossover;
import info.hugoyu.mytraincontrol.switchable.impl.Turnout;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public class SwitchableRegistry {

    private static SwitchableRegistry instance;

    // <decoder address, switchable>
    @Getter
    private final Map<Integer, AbstractSwitchable> switchables;

    private SwitchableRegistry() {
        switchables = new HashMap<>();
    }

    public static SwitchableRegistry getInstance() {
        if (instance == null) {
            instance = new SwitchableRegistry();
        }
        return instance;
    }

    public AbstractSwitchable registerSwitchable(int address, AbstractSwitchable.Type type) {
        AbstractSwitchable switchable = constructSwitchable(address, type)
        switchables.put(address, switchable);
        return switchable;
    }

    private AbstractSwitchable constructSwitchable(int address, AbstractSwitchable.Type type) {
        switch (type) {
            case TURNOUT:
                return new Turnout(address);
            case CROSSOVER:
                return new Crossover(address);
            default:
                throw new RuntimeException("Unrecognized AbstractSwitchable.Type: " + type);
        }
    }

    public AbstractSwitchable getSwitchable(int address) {
        if (!switchables.containsKey(address)) {
            throw new InvalidIdException(address, InvalidIdException.Type.NOT_FOUND);
        }

        return switchables.get(address);
    }
}
