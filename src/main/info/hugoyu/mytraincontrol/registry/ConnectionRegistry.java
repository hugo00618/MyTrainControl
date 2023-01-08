package info.hugoyu.mytraincontrol.registry;

import java.util.HashMap;
import java.util.Map;

public class ConnectionRegistry {
    // from, to, cost
    private Map<Long, Map<Long, Integer>> uplinkConnections = new HashMap<>();
    private Map<Long, Map<Long, Integer>> downlinkConnections = new HashMap<>();

    public void addConnection(long id0, long id1, int cost, boolean isUplink, boolean isBidirectional) {
        addConnection(id0, id1, cost, isUplink);

        if (isBidirectional) {
            addConnection(id1, id0, cost, !isUplink);
        }
    }

    private void addConnection(long id0, long id1, int cost, boolean isUplink) {
        Map<Long, Map<Long, Integer>> connections = isUplink ? uplinkConnections : downlinkConnections;
        connections.putIfAbsent(id0, new HashMap<>());
        connections.get(id0).put(id1, cost);
    }

    public Map<Long, Integer> getNextNodes(long id, boolean isUplink) {
        Map<Long, Map<Long, Integer>> connections = isUplink ? uplinkConnections : downlinkConnections;
        return connections.get(id);
    }
}
