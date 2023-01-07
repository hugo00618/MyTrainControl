package info.hugoyu.mytraincontrol.registry.switchable;

import info.hugoyu.mytraincontrol.switchable.Switchable;

import java.util.Map;

public abstract class AbstractSwitchableRegistry {
    public abstract Map<Integer, ? extends Switchable> getSwitchableRegistry();
}
