package info.hugoyu.mytraincontrol.layout.node;

import java.util.List;

public abstract class AbstractTrackNode implements Allocatable {

    protected AbstractTrackNode() {

    }
    public abstract List<Connection> getConnections();

    public abstract List<Long> getIds();

}
