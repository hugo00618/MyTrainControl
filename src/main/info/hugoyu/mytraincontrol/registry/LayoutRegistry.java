package info.hugoyu.mytraincontrol.registry;

import info.hugoyu.mytraincontrol.exception.InvalidIdException;
import info.hugoyu.mytraincontrol.layout.Connection;
import info.hugoyu.mytraincontrol.layout.Vector;
import info.hugoyu.mytraincontrol.layout.alias.Station;
import info.hugoyu.mytraincontrol.layout.node.AbstractTrackNode;

import java.util.HashMap;
import java.util.Map;

public class LayoutRegistry {

    private static LayoutRegistry instance;

    private Map<Vector, AbstractTrackNode> nodes = new HashMap<>();
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
        node.getConnections().forEach(connection -> {
            // register node
            registerGraphNode(connection, node);

            // add all node connections to the layout
            addConnection(connection);
        });
    }

    private void registerGraphNode(Connection connection, AbstractTrackNode node) {
        nodes.put(connection.getVector(), node);
        if (connection.isBidirectional()) {
            nodes.put(connection.getVector().reversed(), node);
        }
    }

    public void registerAlias(Station station) {
        if (aliases.containsKey(station.getId())) {
            throw new InvalidIdException(station.getId(), InvalidIdException.Type.DUPLICATE);

        }
        aliases.put(station.getId(), station);
    }

    public void addConnection(Connection connection) {
        connectionRegistry.addConnection(
                connection.getVector().getId0(),
                connection.getVector().getId1(),
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

    public AbstractTrackNode getNode(Vector vector) {
        return nodes.get(vector);
    }

    public Station getStation(String id) {
        return aliases.get(id);
    }

    public Map<String, Station> getStations() {
        return aliases;
    }
}
