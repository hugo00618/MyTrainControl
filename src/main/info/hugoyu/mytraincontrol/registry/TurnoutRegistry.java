package info.hugoyu.mytraincontrol.registry;

import info.hugoyu.mytraincontrol.exception.InvalidIdException;
import info.hugoyu.mytraincontrol.json.layout.TurnoutJson;
import info.hugoyu.mytraincontrol.turnout.Turnout;

import java.util.HashMap;
import java.util.Map;

public class TurnoutRegistry {

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

    public void registerTurnout(TurnoutJson turnoutJson) {
        final int address = turnoutJson.getAddress();
        turnouts.put(address, new Turnout(address));
    }

    public Turnout getTurnout(int address) {
        if (!turnouts.containsKey(address)) {
            throw new InvalidIdException(address);
        }

        return turnouts.get(address);
    }

    /**
     *
     * @param address
     * @param state
     * @return if turnout state has changed
     */
    public boolean setTurnoutState(int address, Turnout.State state) {
        boolean isTurnoutStateChanged = false;

        Turnout turnout = this.getTurnout(address);
        Turnout.State cachedState = turnout.getState();
        if (cachedState != state) {
            isTurnoutStateChanged = true;
        }
        turnout.setState(state);

        return isTurnoutStateChanged;
    }
}
