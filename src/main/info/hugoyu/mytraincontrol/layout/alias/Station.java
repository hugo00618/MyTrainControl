package info.hugoyu.mytraincontrol.layout.alias;

import info.hugoyu.mytraincontrol.exception.InvalidIdException;
import info.hugoyu.mytraincontrol.json.layout.StationJson;
import info.hugoyu.mytraincontrol.layout.Route;
import info.hugoyu.mytraincontrol.layout.node.impl.StationTrackNode;
import info.hugoyu.mytraincontrol.util.RouteUtil;
import lombok.Getter;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@Getter
public class Station {
    private String id;
    private String name;
    private List<StationTrackNode> stationTrackNodes;
    private List<Long> entryNodeIds;
    private final Object stationLock = new Object();

    public Station(String id, String name, List<StationTrackNode> stationTrackNodes, List<Long> entryNodeIds) {
        this.id = id;
        this.name = name;
        this.stationTrackNodes = stationTrackNodes;
        this.entryNodeIds = entryNodeIds;

        stationTrackNodes.forEach(stationTrackNode -> stationTrackNode.setStation(this));
    }

    public Station(StationJson stationJson, List<StationTrackNode> stationTrackNodes) {
        this(stationJson.getId(),
                stationJson.getName(),
                stationTrackNodes,
                stationJson.getEntryNodeIds());
    }

    /**
     * Finds the route to the station track that is currently available
     * Will block if no track is available
     *
     * @param isPassingTrackRequired
     * @return
     */
    public Route findRouteToAvailableTrack(long entryNodeId, boolean isPassingTrackRequired) {
        while (true) {
            Route route = getRouteToAvailableTrack(entryNodeId, isPassingTrackRequired);
            if (route == null) {
                synchronized (stationLock) {
                    try {
                        stationLock.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                return route;
            }
        }
    }

    private Route getRouteToAvailableTrack(long entryNodeId, boolean isPassingTrackRequired) {
        if (!entryNodeIds.contains(entryNodeId)) {
            throw new InvalidIdException(entryNodeId, InvalidIdException.Type.NOT_FOUND);
        }

        Stream<StationTrackNode> stationTrackNodesStream = stationTrackNodes.stream();
        if (isPassingTrackRequired) {
            stationTrackNodesStream = stationTrackNodesStream
                    .filter(stationTrackNode -> stationTrackNode.isPassingTrack() && stationTrackNode.isFree());
        } else {
            stationTrackNodesStream = stationTrackNodesStream
                    .filter(StationTrackNode::isFree);
        }
        return stationTrackNodesStream
                // put non-passing track at the front
                .sorted((o1, o2) -> Boolean.compare(o1.isPassingTrack(), o2.isPassingTrack()))
                .map(stationTrackNode -> RouteUtil.findRoute(entryNodeId, stationTrackNode))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    public void broadcast() {
        synchronized (stationLock) {
            stationLock.notifyAll();
        }
    }
}
