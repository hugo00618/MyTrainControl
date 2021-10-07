package info.hugoyu.mytraincontrol.util;

import info.hugoyu.mytraincontrol.layout.Route;
import info.hugoyu.mytraincontrol.layout.alias.Station;
import info.hugoyu.mytraincontrol.layout.node.AbstractTrackNode;
import info.hugoyu.mytraincontrol.layout.node.impl.StationTrackNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class RouteUtil {

    public static Route findRouteToStation(long fromStationTrackNodeId, String to) {
        Station station = LayoutUtil.getStation(to);

        return station.getEntryNodeIds().stream()
                .map(entryNodeId -> findRoute(fromStationTrackNodeId, entryNodeId))
                .filter(Objects::nonNull)
                .min(Route::compareTo)
                .orElse(null);
    }

    public static Route findInboundRoute(long entryNodeId, StationTrackNode stationTrackNode) {
        return findRoute(entryNodeId, stationTrackNode.getId1());
    }

    private static Route findRoute(long from, long to) {
        return findRouteRecur(LayoutUtil.getNode(from), LayoutUtil.getNode(to), new ArrayList<>(), 0);
    }

    private static Route findRouteRecur(AbstractTrackNode node, AbstractTrackNode destination,
                                        List<Long> visited, int cost) {
        if (visited.contains(node.getId())) {
            return null;
        }
        visited.add(node.getId());

        if (node == destination) {
            return new Route(new ArrayList<>(visited), cost);
        }

        Route result = node.getNextNodes().entrySet().stream()
                .map(entry -> findRouteRecur(LayoutUtil.getNode(entry.getKey()), destination, visited, cost + entry.getValue()))
                .filter(Objects::nonNull)
                .min(Route::compareTo)
                .orElse(null);

        visited.remove(visited.size() - 1);

        return result;
    }

}
