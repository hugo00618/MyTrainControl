package info.hugoyu.mytraincontrol.switchable.impl;

import info.hugoyu.mytraincontrol.commandstation.task.AbstractCommandStationTask;
import info.hugoyu.mytraincontrol.commandstation.task.impl.SetThrottleTask;
import info.hugoyu.mytraincontrol.exception.InvalidIdException;
import info.hugoyu.mytraincontrol.registry.ThrottleRegistry;
import info.hugoyu.mytraincontrol.switchable.AbstractSwitchable;
import jmri.DccThrottle;

import java.util.AbstractMap;

import static info.hugoyu.mytraincontrol.switchable.AbstractSwitchable.State.CLOSED;

public class Crossover extends AbstractSwitchable {

    private static final long CROSSOVER_SWITCH_DELAY_MILLIS = 500;
    private static final long HIGH_CURRENT_OCCUPANCY_PERIOD = 750;

    public Crossover(int address) {
        super(address);
    }

    @Override
    public AbstractCommandStationTask getSwitchControlTask() {
        DccThrottle crossoverThrottle = getCrossoverThrottle();

        int throttlePercent = state == CLOSED ? 100 : -100;
        return new SetThrottleTask(
                crossoverThrottle,
                throttlePercent,
                HIGH_CURRENT_OCCUPANCY_PERIOD,
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
