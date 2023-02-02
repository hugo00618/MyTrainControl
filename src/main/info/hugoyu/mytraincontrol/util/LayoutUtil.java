package info.hugoyu.mytraincontrol.util;

import info.hugoyu.mytraincontrol.exception.InvalidIdException;
import info.hugoyu.mytraincontrol.json.layout.LayoutJson;
import info.hugoyu.mytraincontrol.json.layout.LayoutProvider;
import info.hugoyu.mytraincontrol.layout.Vector;
import info.hugoyu.mytraincontrol.layout.alias.Station;
import info.hugoyu.mytraincontrol.layout.node.AbstractTrackNode;
import info.hugoyu.mytraincontrol.layout.node.impl.StationTrackNode;
import info.hugoyu.mytraincontrol.registry.LayoutRegistry;
import lombok.extern.log4j.Log4j;

import java.io.IOException;
import java.util.Map;

@Log4j
public class LayoutUtil {

    private static final String LAYOUT_JSON_PATH = "json-layout-profiles/layout.json";

    public static void registerLayout() {
        registerLayout(LAYOUT_JSON_PATH);
    }

    public static void registerLayout(String filePath) {
        try {
            LayoutJson layoutJson = JsonUtil.parseJSON(filePath, LayoutJson.class);
            LayoutProvider.registerLayout(layoutJson);
        } catch (IOException e) {
            throw new RuntimeException("Error parsing layout", e);
        }
    }

    public static AbstractTrackNode getNode(Vector vector) {
        AbstractTrackNode node = LayoutRegistry.getInstance().getNode(vector);
        if (node == null) {
            throw new InvalidIdException(vector.toString(), InvalidIdException.Type.NOT_FOUND);
        }
        return node;
    }

    public static AbstractTrackNode getNode(long id0, long id1) {
        return getNode(new Vector(id0, id1));
    }

    public static StationTrackNode getStationTrackNode(Vector vector) {
        try {
            return (StationTrackNode) getNode(vector);
        } catch (ClassCastException e) {
            throw new InvalidIdException(vector.toString(), InvalidIdException.Type.NOT_FOUND);
        }
    }

    public static Station getStation(String id) {
        Station station = LayoutRegistry.getInstance().getStation(id);
        if (station == null) {
            throw new InvalidIdException(id, InvalidIdException.Type.NOT_FOUND);
        }
        return station;
    }

    /**
     * Get station from entry node id
     *
     * @param entryNodeId
     * @return
     */
    public static Station getStation(long entryNodeId) {
        return LayoutRegistry.getInstance().getStations().values().stream()
                .filter(station ->
                        (station.getUplinkEntryNode() != null && station.getUplinkEntryNode().equals(entryNodeId)) ||
                                (station.getDownlinkEntryNode() != null && station.getDownlinkEntryNode().equals(entryNodeId)))
                .findFirst()
                .orElse(null);
    }

    public static Map<String, Station> getStations() {
        return LayoutRegistry.getInstance().getStations();
    }

}
