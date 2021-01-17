package info.hugoyu.mytraincontrol.layout;

import info.hugoyu.mytraincontrol.json.JsonParsable;
import info.hugoyu.mytraincontrol.layout.node.AbstractGraphNode;
import info.hugoyu.mytraincontrol.layout.node.impl.RegularNode;
import info.hugoyu.mytraincontrol.layout.node.impl.StationNode;
import lombok.Getter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class Layout implements JsonParsable {

    private List<RegularTrack> regularTracks;
    private Map<String, Station> stations;

    private Map<String, AbstractGraphNode> nodes = new HashMap<>();
    private Map<String, StationTrack> stationTracks = new HashMap<>();

    @Override
    public void postDeserialization() {
        // regular nodes
        for (RegularTrack regularTrack : regularTracks) {
            AbstractGraphNode n0 = new RegularNode(regularTrack.id0, regularTrack.id1);
            registerGraphNode(regularTrack.id0, n0);
        }

        // station nodes
        for (Station station : stations.values()) {
            for (StationTrack track : station.getTracks()) {
                stationTracks.put(track.getId(), track);

                int cost = track.getLength();
                StationTrack.ConnectingNode inboundNode = track.getInboundNode();
                String inboundNodeId = inboundNode.getId();
                AbstractGraphNode stationNode = new StationNode(inboundNodeId, cost);
                registerGraphNode(inboundNodeId, stationNode);

                StationTrack.ConnectingNode outboundNode = track.getOutboundNode();
                stationNode.add(String.valueOf(outboundNode.getId()), cost);
            }
        }
    }

    private void registerGraphNode(long id, AbstractGraphNode node) {
        registerGraphNode(String.valueOf(id), node);
    }

    private void registerGraphNode(String id, AbstractGraphNode node) {
        if (nodes.containsKey(id)) {
            throw new IllegalArgumentException("Duplicated GraphNode id: " + id);
        }
        nodes.put(id, node);
        node.setId(id);
    }

}
