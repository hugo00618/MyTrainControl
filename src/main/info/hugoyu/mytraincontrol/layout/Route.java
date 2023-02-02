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

    public Vector getDestinationVector() {
        return new Vector(
                nodes.get(nodes.size() - 2),
                nodes.get(nodes.size() - 1));
    }
}
