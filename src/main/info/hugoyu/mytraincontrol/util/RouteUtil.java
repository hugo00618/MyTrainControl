package info.hugoyu.mytraincontrol.util;

import info.hugoyu.mytraincontrol.layout.Route;
import info.hugoyu.mytraincontrol.layout.alias.Station;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RouteUtil {

    public static Route findRouteToNode(long from, long to) {
        return findRouteRecur(from, to, new ArrayList<>(), 0);
    }

    public static Route findRouteToStation(long from, String to) {
        Station station = LayoutUtil.getStation(to);

        return station.getEntryNodeIds().stream()
                .map(entryNodeId -> findRouteRecur(from, entryNodeId, new ArrayList<>(), 0))
                .filter(route -> route != null)
                .sorted()
                .findFirst()
                .orElse(null);
    }

    private static Route findRouteRecur(long nodeId, long destinationId, List<Long> visited, int cost) {
        if (visited.contains(nodeId)) {
            return null;
        }
        visited.add(nodeId);

        if (nodeId == destinationId) {
            return new Route(new ArrayList<>(visited), cost);
        }

        Route result = null;
        for (Map.Entry<Long, Integer> nextNode : LayoutUtil.getNode(nodeId).getNextNodes().entrySet()) {
            Route route = findRouteRecur(nextNode.getKey(), destinationId, visited, cost + nextNode.getValue());
            if (route != null && (result == null || result.getCost() > route.getCost())) {
                result = route;
            }
        }

        visited.remove(visited.size() - 1);

        return result;
    }

}
