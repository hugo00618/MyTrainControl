package info.hugoyu.mytraincontrol.layout.alias;

import info.hugoyu.mytraincontrol.json.layout.StationJson;
import info.hugoyu.mytraincontrol.layout.node.impl.StationTrackNode;
import lombok.Getter;

import java.util.List;

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

    public void waitForStationTrack() throws InterruptedException {
        synchronized (stationLock) {
            stationLock.wait();
        }
    }
    public void broadcast() {
        synchronized (stationLock) {
            stationLock.notifyAll();
        }
    }
}
