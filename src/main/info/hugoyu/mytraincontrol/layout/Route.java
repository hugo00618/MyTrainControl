package info.hugoyu.mytraincontrol.layout;

import info.hugoyu.mytraincontrol.util.LayoutUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
public class Route implements Comparable<Route> {

    @Getter
    private List<Long> nodes;

    private int cost;

    @Override
    public int compareTo(Route o) {
        return this.cost - o.cost;
    }

    /**
     *
     * @return move distance (disregarding the outbound distance)
     */
    public int getMoveDist() {
        int outboundDist = LayoutUtil.getNode(nodes.get(0)).getNextNodes().get(nodes.get(1));
        return cost - outboundDist;
    }
}
