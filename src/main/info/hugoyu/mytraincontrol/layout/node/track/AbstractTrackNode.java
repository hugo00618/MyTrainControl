package info.hugoyu.mytraincontrol.layout.node.track;

import info.hugoyu.mytraincontrol.layout.Allocatable;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Setter
@Getter
public abstract class AbstractTrackNode implements Allocatable {

    protected long id;

    protected Map<Long, Integer> nextNodes = new HashMap<>();

    protected AbstractTrackNode(long id) {
        this.id = id;
    }

    public abstract String getOwnerStatus(int ownerId);

    public void addConnection(long nextNode, int cost) {
        nextNodes.put(nextNode, cost);
    }
}
