package info.hugoyu.mytraincontrol.util;

import info.hugoyu.mytraincontrol.layout.Route;
import info.hugoyu.mytraincontrol.layout.alias.Station;
import info.hugoyu.mytraincontrol.layout.node.AbstractTrackNode;
import info.hugoyu.mytraincontrol.layout.node.impl.StationTrackNode;
import info.hugoyu.mytraincontrol.trainset.Trainset;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RouteUtil {

    public static Route findRouteToStation(Trainset trainset, AbstractTrackNode from, String toStation) {
        Station station = LayoutUtil.getStation(toStation);

        return station.getStationTrackNodes().stream()
                .filter(stationTrackNode -> stationTrackNode.isPlatformTrackAbleToFit(trainset))
                .flatMap(stationTrackNode -> Stream.ofNullable(findRoute(from, stationTrackNode)))
                .min(Route::compareTo)
                .orElse(null);
    }

    public static List<Route> findReachableStations(Trainset trainset) {
        AbstractTrackNode fromStationTrackNode = LayoutUtil.getNode(trainset.getLastAllocatedNode());
        return LayoutUtil.getStations().keySet().stream()
                .flatMap(station -> Stream.ofNullable(findRouteToStation(trainset, fromStationTrackNode, station)))
                .collect(Collectors.toList());
    }

    /**
     * Finds the route to the station track that is currently available
     * Will block if no track is available
     *
     * @param isPassingTrackRequired
     * @return
     */
    public static Route findRouteToAvailableStationTrack(Trainset trainset, long entryNodeId,
                                                         boolean isPassingTrackRequired, boolean isPlatformTrackRequired) {
        Station station = LayoutUtil.getStation(entryNodeId);
        while (true) {
            Route route = getRouteToAvailableTrack(trainset, entryNodeId, isPassingTrackRequired, isPlatformTrackRequired);
            if (route == null) {
                try {
                    station.waitForStationTrack();
                } catch (InterruptedException e) {
                    throw new RuntimeException("Error waiting for station track");
                }
            } else {
                return route;
            }
        }
    }

    private static Route getRouteToAvailableTrack(Trainset trainset, long entryNodeId,
                                                  boolean isPassingTrackRequired, boolean isPlatformTrackRequired) {
        Station station = LayoutUtil.getStation(entryNodeId);
        return station.getStationTrackNodes().stream()
                .filter(StationTrackNode::isFree)
                // filter by isPassingTrack() if isPassingTrackRequired
                .filter(stationTrackNode -> !isPassingTrackRequired || stationTrackNode.isPassingTrack())
                // filter by isPlatformTrackAbleToFit() if isPlatformTrackRequired
                .filter(stationTrackNode -> !isPlatformTrackRequired || stationTrackNode.isPlatformTrackAbleToFit(trainset))
                // give preference to non-Passing track
                .sorted((o1, o2) -> Boolean.compare(o1.isPassingTrack(), o2.isPassingTrack()))
                .map(stationTrackNode -> RouteUtil.findRoute(LayoutUtil.getNode(entryNodeId), stationTrackNode))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    public static Route findRoute(AbstractTrackNode from, AbstractTrackNode to) {
        Route uplinkRoute = findRoute(from, to, true),
                downlinkRoute = findRoute(from, to, false);

        if (uplinkRoute == null || downlinkRoute == null) {
            return uplinkRoute == null ? downlinkRoute : uplinkRoute;
        }

        return uplinkRoute.compareTo(downlinkRoute) < 0 ? uplinkRoute : downlinkRoute;
    }

    public static Route findRoute(long from, long to) {
        return findRoute(LayoutUtil.getNode(from), LayoutUtil.getNode(to));
    }

    public static Route findRoute(AbstractTrackNode from, AbstractTrackNode to, boolean isUplink) {
        return findRouteRecur(from, to, isUplink, new ArrayList<>(), 0);
    }

    public static Route findRoute(long from, long to, boolean isUplink) {
        return findRoute(LayoutUtil.getNode(from), LayoutUtil.getNode(to), isUplink);
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

}
