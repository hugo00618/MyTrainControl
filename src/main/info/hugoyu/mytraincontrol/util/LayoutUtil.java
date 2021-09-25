package info.hugoyu.mytraincontrol.util;

import info.hugoyu.mytraincontrol.exception.NodeAllocationException;
import info.hugoyu.mytraincontrol.json.MyJsonReader;
import info.hugoyu.mytraincontrol.json.layout.LayoutJson;
import info.hugoyu.mytraincontrol.json.layout.LayoutProvider;
import info.hugoyu.mytraincontrol.layout.BlockSectionResult;
import info.hugoyu.mytraincontrol.layout.alias.Station;
import info.hugoyu.mytraincontrol.layout.node.track.AbstractTrackNode;
import info.hugoyu.mytraincontrol.registry.LayoutRegistry;
import info.hugoyu.mytraincontrol.trainset.Trainset;
import lombok.extern.log4j.Log4j;

import java.io.IOException;
import java.util.Map;

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
            log.error("Error parsing layout", e);
        }
    }

    public static AbstractTrackNode getNode(long id) {
        AbstractTrackNode node = LayoutRegistry.getInstance().getNode(id);
        if (node == null) {
            throw new RuntimeException("Invalid node id: " + id);
        }
        return node;
    }

    public static Station getStation(String id) {
        Station station = LayoutRegistry.getInstance().getStation(id);
        if (station == null) {
            throw new RuntimeException("Invalid station id: " + id);
        }
        return station;
    }

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

}
