package info.hugoyu.mytraincontrol.layout.node;

import info.hugoyu.mytraincontrol.layout.Connection;
import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
public abstract class AbstractTrackNode implements Allocatable {

    protected final boolean isBidirectional;

    @Override
    public final boolean isBidirectional() {
        return isBidirectional;
    }

    public abstract List<Connection> getConnections();
}
