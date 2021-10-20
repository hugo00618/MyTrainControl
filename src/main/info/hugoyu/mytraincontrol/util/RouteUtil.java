package info.hugoyu.mytraincontrol.util;

import info.hugoyu.mytraincontrol.layout.Route;
import info.hugoyu.mytraincontrol.layout.alias.Station;
import info.hugoyu.mytraincontrol.layout.node.AbstractTrackNode;
import info.hugoyu.mytraincontrol.layout.node.impl.StationTrackNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

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
        return findRoute(entryNodeId, stationTrackNode);
    }

    public static Route findRoute(Object from, Object to) {
        Route uplinkRoute = findRoute(from, to, true), downlinkRoute = findRoute(from, to, false);

        if (uplinkRoute == null) {
            return downlinkRoute;
        }
        if (downlinkRoute == null) {
            return uplinkRoute;
        }

        return uplinkRoute.compareTo(downlinkRoute) < 0 ? uplinkRoute : downlinkRoute;
    }

    public static Route findRoute(Object from, Object to, boolean isUplink) {
        AbstractTrackNode fromNode = convertToNode(from), toNode = convertToNode(to);
        return findRouteRecur(fromNode, toNode, isUplink, new ArrayList<>(), 0);
    }

    private static Route findRouteRecur(AbstractTrackNode node, AbstractTrackNode destination, boolean isUplink,
                                        List<Long> visited, int cost) {
        if (visited.contains(node.getId())) {
            return null;
        }
        Long previousNodeId = visited.isEmpty() ? null : visited.get(visited.size() - 1);
        visited.add(node.getId());

        if (node == destination) {
            return new Route(new ArrayList<>(visited), cost, isUplink);
        }

        Set<Long> nextNodes = isUplink ? node.getUplinkNextNodes() : node.getDownlinkNextNodes();
        Route result = nextNodes.stream()
                .map(nextNodeId -> findRouteRecur(LayoutUtil.getNode(nextNodeId), destination, isUplink, visited,
                        cost + node.getCostToNode(nextNodeId, previousNodeId)))
                .filter(Objects::nonNull)
                .min(Route::compareTo)
                .orElse(null);

        visited.remove(visited.size() - 1);

        return result;
    }

    private static AbstractTrackNode convertToNode(Object node) {
        return node instanceof AbstractTrackNode ?
                (AbstractTrackNode) node :
                LayoutUtil.getNode((Long) node);
    }

}
