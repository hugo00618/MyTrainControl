package info.hugoyu.mytraincontrol.layout;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;

@Getter
public class Route {

    List<GraphNode> routeNodes;
    int cost = 0;

    public Route(GraphNode origin) {
        routeNodes = Arrays.asList(origin);
    }

    public void add(GraphNode node) {
        GraphNode lastNode = routeNodes.get(routeNodes.size() - 1);
        cost += lastNode.getCostToNextNode(node);
        routeNodes.add(node);
    }

}
