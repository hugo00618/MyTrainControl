package info.hugoyu.mytraincontrol.switchable.impl;

import info.hugoyu.mytraincontrol.commandstation.task.AbstractCommandStationTask;
import info.hugoyu.mytraincontrol.commandstation.task.RecursiveCommandStationTask;
import info.hugoyu.mytraincontrol.commandstation.task.impl.SetThrottleTask;
import info.hugoyu.mytraincontrol.exception.InvalidIdException;
import info.hugoyu.mytraincontrol.registry.ThrottleRegistry;
import info.hugoyu.mytraincontrol.switchable.Switchable;
import jmri.DccThrottle;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

import static info.hugoyu.mytraincontrol.switchable.Switchable.State.CLOSED;

@Setter
@Getter
public class Crossover implements Switchable {

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
        return new RecursiveCommandStationTask(List.of(
                new SetThrottleTask(crossoverThrottle, throttlePercent),
                new SetThrottleTask(crossoverThrottle, 0, 100)
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
