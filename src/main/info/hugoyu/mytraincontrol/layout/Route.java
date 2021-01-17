package info.hugoyu.mytraincontrol.layout;

import info.hugoyu.mytraincontrol.util.LayoutUtil;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
public class Route implements Comparable<Route> {

    List<String> routeNodes;

    public Route(String origin) {
        routeNodes = new ArrayList<>(Arrays.asList(origin));
    }

    public void add(String nodeId) {
        routeNodes.add(nodeId);
    }

    public int getCost() {
        int res = 0;
        for (int i = 1; i < routeNodes.size(); i++) {
            String id0 = routeNodes.get(i - 1);
            String id1 = routeNodes.get(i);
            res += LayoutUtil.getNode(id0).getCostToNextNode(id1);
        }
        return res;
    }

    @Override
    public int compareTo(Route o) {
        return this.getCost() - o.getCost();
    }
}
