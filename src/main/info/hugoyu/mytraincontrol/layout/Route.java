package info.hugoyu.mytraincontrol.layout;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class Route implements Comparable<Route> {

    private List<Long> nodes;
    private int cost;
    private boolean isUplink;

    @Override
    public int compareTo(Route o) {
        return this.cost - o.cost;
    }

    public long getDestinationNode() {
        return nodes.get(nodes.size() - 1);
    }
}
