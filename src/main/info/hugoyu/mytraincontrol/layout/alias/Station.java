package info.hugoyu.mytraincontrol.layout.alias;

import info.hugoyu.mytraincontrol.json.layout.StationJson;
import info.hugoyu.mytraincontrol.layout.node.impl.StationTrackNode;
import lombok.Getter;

import java.util.List;

@Getter
public class Station {
    private final String id;
    private final String name;
    private final List<StationTrackNode> stationTrackNodes;
    private final Long uplinkEntryNode, downlinkEntryNode;

    private final Object stationLock = new Object();

    public Station(String id,
                   String name,
                   List<StationTrackNode> stationTrackNodes,
                   Long uplinkEntryNode,
                   Long downlinkEntryNode) {
        this.id = id;
        this.name = name;
        this.stationTrackNodes = stationTrackNodes;
        this.uplinkEntryNode = uplinkEntryNode;
        this.downlinkEntryNode = downlinkEntryNode;

        stationTrackNodes.forEach(stationTrackNode -> stationTrackNode.setStation(this));
    }

    public Station(StationJson stationJson, List<StationTrackNode> stationTrackNodes) {
        this(stationJson.getId(),
                stationJson.getName(),
                stationTrackNodes,
                stationJson.getUplinkEntryNode(),
                stationJson.getDownlinkEntryNode());
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
