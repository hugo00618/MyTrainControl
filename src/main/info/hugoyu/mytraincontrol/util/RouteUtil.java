package info.hugoyu.mytraincontrol.util;

import info.hugoyu.mytraincontrol.layout.Route;
import info.hugoyu.mytraincontrol.layout.Station;
import info.hugoyu.mytraincontrol.layout.node.AbstractGraphNode;
import info.hugoyu.mytraincontrol.layout.node.impl.StationNode;
import info.hugoyu.mytraincontrol.trainset.Trainset;

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class RouteUtil {

    private static Map<String, AbstractGraphNode> nodes;
    private static Map<String, Station> stations;

    static {
        nodes = LayoutUtil.getNodes();
        stations = LayoutUtil.getStations();
    }

    private static Route findRouteToNode(String fromNode, String toNode) {
        if (nodes.get(fromNode).getMinCostToNode(toNode) == Integer.MAX_VALUE) {
            if (calculateCost(fromNode, toNode, new HashSet<>()) == -1) {
                return null;
            }
        }

        Route res = new Route(fromNode);
        String it = fromNode;
        while (!it.equals(toNode)) {
            String minCostNodeId = nodes.get(it).getMinCostNextNode(toNode);
            res.add(minCostNodeId);
            it = minCostNodeId;
        }
        return res;
    }

    public static Route findRouteToStation(Trainset trainset, String fromNode, String toStation) {
        int trainLength = trainset.getProfile().getTotalLength();
        return stations.get(toStation).getInboundNodes().stream()
                .filter(inboundNode -> isTrackFitTrain(String.valueOf(inboundNode.getId()), trainLength))
                .map(inboundNode -> findRouteToNode(fromNode, String.valueOf(inboundNode.getId())))
                .filter(Objects::nonNull)
                .sorted()
                .findFirst()
                .orElse(null);
    }

    private static boolean isTrackFitTrain(String nodeId, int trainLength) {
        StationNode stationNode = (StationNode) LayoutUtil.getNode(nodeId);
        return stationNode.getLength() >= trainLength;
    }

    private static int calculateCost(String from, String to, Set<String> visited) {
        if (from.equals(to)) {
            return 0;
        }

        if (visited.contains(from)) {
            return -1;
        }

        visited.add(from);
        AbstractGraphNode fromNode = nodes.get(from);
        for (String next : fromNode.getNextNodes()) {
            int cost = fromNode.getCostToNextNode(next);
            int nextCost = calculateCost(next, to, visited);
            if (nextCost == -1) { // unreachable
                return -1;
            }
            fromNode.setMinCostToNode(to, next, cost + nextCost);
        }
        visited.remove(from);

        return fromNode.getMinCostToNode(to);
    }

}
