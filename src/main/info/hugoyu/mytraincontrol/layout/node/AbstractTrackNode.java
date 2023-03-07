package info.hugoyu.mytraincontrol.layout.node;

import info.hugoyu.mytraincontrol.layout.Connection;
import lombok.AllArgsConstructor;

import java.util.Set;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@AllArgsConstructor
public abstract class AbstractTrackNode implements Allocatable {

    protected final boolean isBidirectional;

    protected final Lock occupierLock = new ReentrantLock();
    protected final Condition occupierChangeCondition = occupierLock.newCondition();

    @Override
    public final boolean isBidirectional() {
        return isBidirectional;
    }

    @Override
    public final Lock getOccupierLock() {
        return occupierLock;
    }

    @Override
    public final Condition getOccupierChangeCondition() {
        return occupierChangeCondition;
    }

    public abstract Set<Connection> getConnections();
}
