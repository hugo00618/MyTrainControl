package info.hugoyu.mytraincontrol.util;

import info.hugoyu.mytraincontrol.layout.GraphNode;
import info.hugoyu.mytraincontrol.layout.Route;

import java.util.HashSet;
import java.util.Set;

public class RouteUtil {

    public static Route findRoute(GraphNode from, GraphNode to) {
        if (from.getMinCostToNode(to) == Integer.MAX_VALUE) {
            if (calculateCost(from, to, new HashSet<>()) == -1) {
                return null;
            }
        }

        Route res = new Route(from);
        GraphNode it = from;
        while (it != to) {
            GraphNode minCostNode = it.getMinCostNextNode(to);
            res.add(minCostNode);
            it = minCostNode;
        }
        return res;
    }

    private static int calculateCost(GraphNode from, GraphNode to, Set<GraphNode> visited) {
        if (from == to) {
            return 0;
        }

        if (visited.contains(from)) {
            return -1;
        }

        visited.add(from);
        for (GraphNode nextNode : from.getNextNodes()) {
            int cost = from.getCostToNextNode(nextNode);
            int nextCost = calculateCost(nextNode, to, visited);
            if (nextCost == -1) { // unreachable
                return -1;
            }
            from.setMinCostToNode(to, nextNode, cost + nextCost);
        }
        visited.remove(from);

        return from.getMinCostToNode(to);
    }

}
