package info.hugoyu.mytraincontrol.switchable.impl;

import info.hugoyu.mytraincontrol.commandstation.task.AbstractCommandStationTask;
import info.hugoyu.mytraincontrol.commandstation.task.impl.SetThrottleTask;
import info.hugoyu.mytraincontrol.exception.InvalidIdException;
import info.hugoyu.mytraincontrol.registry.ThrottleRegistry;
import info.hugoyu.mytraincontrol.switchable.Switchable;
import jmri.DccThrottle;
import lombok.Getter;
import lombok.Setter;

import java.util.AbstractMap;

import static info.hugoyu.mytraincontrol.switchable.Switchable.State.CLOSED;

@Setter
@Getter
public class Crossover implements Switchable {

    private static final long CROSSOVER_SWITCH_DELAY_MILLIS = 500;

    private int address;
    private State state;

    public Crossover(int address) {
        this.address = address;
        this.state = State.UNKNOWN;
    }

    @Override
    public AbstractCommandStationTask getSwitchControlTask() {
        DccThrottle crossoverThrottle = getCrossoverThrottle();

        int throttlePercent = state == CLOSED ? 100 : -100;
        return new SetThrottleTask(
                crossoverThrottle,
                throttlePercent,
                CROSSOVER_SWITCH_DELAY_MILLIS,
                new AbstractMap.SimpleImmutableEntry<>(
                        new SetThrottleTask(crossoverThrottle, 0),
                        CROSSOVER_SWITCH_DELAY_MILLIS
                ));
    }

    private DccThrottle getCrossoverThrottle() {
        try {
            ThrottleRegistry.getInstance().registerThrottle(address);
        } catch (InvalidIdException e) {
            // ignore if the crossover throttle is already registered
        }
        return ThrottleRegistry.getInstance().getThrottle(address);
    }

}
