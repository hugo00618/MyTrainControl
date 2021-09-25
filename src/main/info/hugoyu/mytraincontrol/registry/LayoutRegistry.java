package info.hugoyu.mytraincontrol.registry;

import com.google.common.annotations.VisibleForTesting;
import info.hugoyu.mytraincontrol.layout.alias.Station;
import info.hugoyu.mytraincontrol.layout.node.track.AbstractTrackNode;

import java.util.HashMap;
import java.util.Map;

public class LayoutRegistry {

    private static LayoutRegistry instance;

    private Map<Long, AbstractTrackNode> nodes = new HashMap<>();
    private Map<String, Station> aliases = new HashMap<>();

    private LayoutRegistry() {

    }

    public static LayoutRegistry getInstance() {
        if (instance == null) {
            instance = new LayoutRegistry();
        }
        return instance;
    }

    public static LayoutRegistry getNewInstance() {
        instance = new LayoutRegistry();
        return instance;
    }

    public void registerGraphNode(AbstractTrackNode node) {
        if (nodes.containsKey(node.getId())) {
            throw new RuntimeException("Duplicated GraphNode id: " + node.getId());
        }
        nodes.put(node.getId(), node);
    }

    public void registerAlias(Station station) {
        if (aliases.containsKey(station.getId())) {
            throw new RuntimeException("Duplicated Alias id: " + station.getId());
        }
        aliases.put(station.getId(), station);
    }

    public AbstractTrackNode getNode(long id) {
        return nodes.get(id);
    }

    public Station getStation(String id) {
        return aliases.get(id);
    }
    public Map<String, Station> getStations() {
        return aliases;
    }

    @VisibleForTesting
     Map<Long, AbstractTrackNode> getNodes() {
        return nodes;
    }
}
