package info.hugoyu.mytraincontrol.util;

import info.hugoyu.mytraincontrol.exception.InvalidIdException;
import info.hugoyu.mytraincontrol.exception.NodeAllocationException;
import info.hugoyu.mytraincontrol.json.MyJsonReader;
import info.hugoyu.mytraincontrol.json.layout.LayoutJson;
import info.hugoyu.mytraincontrol.json.layout.LayoutProvider;
import info.hugoyu.mytraincontrol.layout.BlockSectionResult;
import info.hugoyu.mytraincontrol.layout.alias.Station;
import info.hugoyu.mytraincontrol.layout.node.AbstractTrackNode;
import info.hugoyu.mytraincontrol.layout.node.impl.StationTrackNode;
import info.hugoyu.mytraincontrol.registry.LayoutRegistry;
import info.hugoyu.mytraincontrol.trainset.Trainset;
import lombok.extern.log4j.Log4j;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Log4j
public class LayoutUtil {

    private static final String LAYOUT_JSON_PATH = "json-layout-profiles/layout.json";

    static {
        registerLayout(LAYOUT_JSON_PATH);
    }

    public static void registerLayout(String layoutJsonPath) {
        try {
            LayoutJson layoutJson = MyJsonReader.parseJSON(layoutJsonPath, LayoutJson.class);
            LayoutProvider.registerLayout(layoutJson);
        } catch (IOException e) {
            throw new RuntimeException("Error parsing layout", e);
        }
    }

    public static AbstractTrackNode getNode(long id) {
        AbstractTrackNode node = LayoutRegistry.getInstance().getNode(id);
        if (node == null) {
            throw new InvalidIdException(id);
        }
        return node;
    }

    public static boolean isStationTrackNode(long id) {
        return getNode(id) instanceof StationTrackNode;
    }

    public static StationTrackNode getStationTrackNode(long id) {
        try {
            return (StationTrackNode) getNode(id);
        } catch (ClassCastException e) {
            throw new InvalidIdException(id);
        }
    }

    public static Station getStation(String id) {
        Station station = LayoutRegistry.getInstance().getStation(id);
        if (station == null) {
            throw new InvalidIdException(id);
        }
        return station;
    }

    /**
     * Get station by entry node id
     *
     * @param entryNodeId
     * @return
     */
    public static Station getStation(long entryNodeId) {
        return LayoutRegistry.getInstance().getStations().values().stream()
                .filter(station -> station.getEntryNodeIds().contains(entryNodeId))
                .findFirst()
                .orElse(null);
    }

    public static Map<String, Station> getStations() {
        return LayoutRegistry.getInstance().getStations();
    }

    public static BlockSectionResult allocNode(long nodeId, Trainset trainset, int dist, Long nextNodeId, Long previousNodeId)
            throws NodeAllocationException {
        return getNode(nodeId).alloc(trainset, dist, nextNodeId, previousNodeId);
    }

    public static BlockSectionResult freeNode(long nodeId, Trainset trainset, int dist) throws NodeAllocationException {
        return getNode(nodeId).free(trainset, dist);
    }

    public static boolean isReachable(long fromId, long toId) {
        AbstractTrackNode fromNode = getNode(fromId), toNode = getNode(toId);
        return isReachableRecur(fromNode, toNode, true, new ArrayList<>()) ||
                isReachableRecur(fromNode, toNode, false, new ArrayList<>());
    }

    private static boolean isReachableRecur(AbstractTrackNode node, AbstractTrackNode toNode, boolean isUplink, List<Long> visited) {
        if (visited.contains(node.getId())) {
            return false;
        }

        if (node == toNode) {
            return true;
        }

        visited.add(node.getId());
        Set<Long> nextNodes = isUplink ? node.getUplinkNextNodes() : node.getDownlinkNextNodes();
        boolean isReachable = nextNodes.stream()
                .anyMatch(nextNodeId -> isReachableRecur(getNode(nextNodeId), toNode, isUplink, visited));
        visited.remove(visited.size() - 1);

        return isReachable;
    }

}
