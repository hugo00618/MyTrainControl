package info.hugoyu.mytraincontrol.util;

import info.hugoyu.mytraincontrol.layout.Route;
import info.hugoyu.mytraincontrol.layout.alias.Station;
import info.hugoyu.mytraincontrol.layout.node.impl.StationTrackNode;
import info.hugoyu.mytraincontrol.registry.LayoutRegistry;
import info.hugoyu.mytraincontrol.trainset.Trainset;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RouteUtil {

    public static Route findRouteToStation(Trainset trainset, long from, String stationAlias) {
        Station station = LayoutUtil.getStation(stationAlias);

        boolean isTrainsetAbleToFit = station.getStationTrackNodes().stream()
                .anyMatch(stationTrackNode -> stationTrackNode.isPlatformTrackAbleToFit(trainset));
        if (isTrainsetAbleToFit) {
            return station.getEntryNodeIds().stream()
                    .flatMap(entryNodeId -> Stream.ofNullable(findRoute(from, entryNodeId)))
                    .min(Route::compareTo)
                    .orElse(null);
        }
        return null;
    }

    public static List<Route> findReachableStations(Trainset trainset) {
        long fromStationTrackNodeId = trainset.getLastAllocatedNodeId();
        return LayoutUtil.getStations().keySet().stream()
                .flatMap(station -> Stream.ofNullable(findRouteToStation(trainset, fromStationTrackNodeId, station)))
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
                // sort by isPassingTrack() and getTrackLength(), give preference to
                //     1. non-Passing track
                //     2. tracks with the lowest possible length
                .sorted(Comparator.comparing(StationTrackNode::isPassingTrack)
                        .thenComparing(StationTrackNode::getTrackLength))
                .map(stationTrackNode -> RouteUtil.findRoute(entryNodeId, stationTrackNode.getIds().get(0)))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    public static Route findRoute(long from, long to) {
        Route uplinkRoute = findRoute(from, to, true),
                downlinkRoute = findRoute(from, to, false);

        if (uplinkRoute == null || downlinkRoute == null) {
            return uplinkRoute == null ? downlinkRoute : uplinkRoute;
        }

        return uplinkRoute.compareTo(downlinkRoute) < 0 ? uplinkRoute : downlinkRoute;
    }

    public static Route findRoute(long from, long to, boolean isUplink) {
        return findRouteRecur(from, to, isUplink, new ArrayList<>(), 0);
    }

    private static Route findRouteRecur(long nodeId, long destinationId, boolean isUplink,
                                        List<Long> visited, int cost) {
        if (visited.contains(nodeId)) {
            return null;
        }
        visited.add(nodeId);

        if (nodeId == destinationId) {
            return new Route(new ArrayList<>(visited), cost, isUplink);
        }

        Map<Long, Integer> nextNodes = LayoutRegistry.getInstance().getNextNodes(nodeId, isUplink);
        Route result = nextNodes.entrySet().stream()
                .map(nextNode -> {
                    final long nextNodeId = nextNode.getKey();
                    final int nextNodeCost = nextNode.getValue();
                    return findRouteRecur(nextNodeId, destinationId, isUplink, visited,
                            cost + nextNodeCost);
                })
                .filter(Objects::nonNull)
                .min(Route::compareTo)
                .orElse(null);

        visited.remove(visited.size() - 1);

        return result;
    }

}
