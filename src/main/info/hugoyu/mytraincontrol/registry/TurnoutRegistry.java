package info.hugoyu.mytraincontrol.registry;

import info.hugoyu.mytraincontrol.util.TurnoutState;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

public class TurnoutRegistry {

    private static TurnoutRegistry instance;

    private Map<Integer, Turnout> turnouts;

    private TurnoutRegistry() {
        turnouts = new HashMap<>();
    }

    static class Turnout {
        final int address;

        @Setter
        TurnoutState state;

        public Turnout(int address) {
            this.address = address;
            state = TurnoutState.UNKNOWN;
        }
    }

    public static TurnoutRegistry getInstance() {
        if (instance == null) {
            instance = new TurnoutRegistry();
        }
        return instance;
    }

    /**
     *
     * @param address
     * @param state
     * @return if turnout state has changed
     */
    public boolean setTurnoutState(int address, TurnoutState state) {
        if (!turnouts.containsKey(address)) {
            turnouts.put(address, new Turnout(address));
        }

        boolean isTurnoutStateChanged = false;
        Turnout turnout = turnouts.get(address);
        if (turnout.state != state) {
            isTurnoutStateChanged = true;
        }
        turnout.setState(state);

        return isTurnoutStateChanged;
    }
}
