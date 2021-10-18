package info.hugoyu.mytraincontrol.layout.node;

import info.hugoyu.mytraincontrol.layout.Allocatable;
import lombok.Getter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class AbstractTrackNode implements Allocatable {

    @Getter
    protected long id;

    @Getter
    protected Set<Long> uplinkNextNodes, downlinkNextNodes;

    protected Map<Long, Integer> costs;

    protected AbstractTrackNode(long id) {
        this.id = id;
        uplinkNextNodes = new HashSet<>();
        downlinkNextNodes = new HashSet<>();
        costs = new HashMap<>();
    }

    public abstract String getOwnerStatus(int ownerId);

    public abstract int getCostToNode(long toNode, Long previousNode);

    public void addConnection(long nextNode, int cost, boolean isUplink) {
        if (isUplink) {
            uplinkNextNodes.add(nextNode);
        } else {
            downlinkNextNodes.add(nextNode);
        }
        costs.put(nextNode, cost);
    }
}
