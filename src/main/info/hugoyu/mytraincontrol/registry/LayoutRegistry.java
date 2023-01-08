package info.hugoyu.mytraincontrol.registry;

import com.google.common.annotations.VisibleForTesting;
import info.hugoyu.mytraincontrol.exception.InvalidIdException;
import info.hugoyu.mytraincontrol.layout.alias.Station;
import info.hugoyu.mytraincontrol.layout.node.AbstractTrackNode;
import info.hugoyu.mytraincontrol.layout.node.Connection;

import java.util.HashMap;
import java.util.Map;

public class LayoutRegistry {

    private static LayoutRegistry instance;

    private Map<Long, AbstractTrackNode> nodes = new HashMap<>();
    private ConnectionRegistry connectionRegistry = new ConnectionRegistry();
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
        node.getIds().forEach(id -> registerGraphNode(id, node));

        // add all node connections to the layout
        node.getConnections().forEach(connection -> LayoutRegistry.getInstance().addConnection(connection));
    }

    private void registerGraphNode(long id, AbstractTrackNode node) {
        if (nodes.containsKey(id)) {
            throw new InvalidIdException(id, InvalidIdException.Type.DUPLICATE);
        }
        nodes.put(id, node);
    }

    public void registerAlias(Station station) {
        if (aliases.containsKey(station.getId())) {
            throw new InvalidIdException(station.getId(), InvalidIdException.Type.DUPLICATE);

        }
        aliases.put(station.getId(), station);
    }

    public void addConnection(Connection connection) {
        connectionRegistry.addConnection(
                connection.getId0(),
                connection.getId1(),
                connection.getDist(),
                connection.isUplink(),
                connection.isBidirectional());
    }

    /**
     * @param id
     * @param isUplink
     * @return nextNodes of id, in <nextNode, cost> format
     */
    public Map<Long, Integer> getNextNodes(long id, boolean isUplink) {
        return connectionRegistry.getNextNodes(id, isUplink);
    }

    public AbstractTrackNode getNode(long id) {
        if (!nodes.containsKey(id)) {
            throw new InvalidIdException(id, InvalidIdException.Type.NOT_FOUND);
        }
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
