package info.hugoyu.mytraincontrol.registry.switchable.impl;

import info.hugoyu.mytraincontrol.exception.InvalidIdException;
import info.hugoyu.mytraincontrol.json.layout.TurnoutJson;
import info.hugoyu.mytraincontrol.registry.switchable.AbstractSwitchableRegistry;
import info.hugoyu.mytraincontrol.switchable.Switchable;
import info.hugoyu.mytraincontrol.switchable.impl.Turnout;

import java.util.HashMap;
import java.util.Map;

public class TurnoutRegistry extends AbstractSwitchableRegistry {

    private static TurnoutRegistry instance;

    private Map<Integer, Turnout> turnouts;

    private TurnoutRegistry() {
        turnouts = new HashMap<>();
    }

    public static TurnoutRegistry getInstance() {
        if (instance == null) {
            instance = new TurnoutRegistry();
        }
        return instance;
    }

    public Turnout registerTurnout(TurnoutJson turnoutJson) {
        final int address = turnoutJson.getAddress();
        Turnout turnout = new Turnout(address);
        turnouts.put(address, turnout);
        return turnout;
    }

    public Turnout getTurnout(int address) {
        if (!turnouts.containsKey(address)) {
            throw new InvalidIdException(address, InvalidIdException.Type.NOT_FOUND);
        }

        return turnouts.get(address);
    }

    /**
     *
     * @param turnout
     * @param state
     * @return if turnout state has changed
     */
    public boolean setTurnoutState(Turnout turnout, Turnout.State state) {
        boolean isTurnoutStateChanged = false;

        Turnout.State cachedState = turnout.getState();
        if (cachedState != state) {
            isTurnoutStateChanged = true;
        }
        turnout.setState(state);

        return isTurnoutStateChanged;
    }

    @Override
    public Map<Integer, ? extends Switchable> getSwitchableRegistry() {
        return turnouts;
    }
}
