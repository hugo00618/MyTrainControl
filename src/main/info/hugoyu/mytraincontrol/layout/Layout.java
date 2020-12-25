package info.hugoyu.mytraincontrol.layout;

import info.hugoyu.mytraincontrol.json.JsonParsable;
import lombok.Getter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class Layout implements JsonParsable {

    class RegularTrack {
        int id0, id1;
    }

    private List<RegularTrack> regularTracks;
    private List<Station> stations;

    private Map<String, GraphNode> nodes = new HashMap<>();

    @Override
    public void postDeserialization() {
        // add regular nodes
        for (RegularTrack regularTrack : regularTracks) {
            int cost = regularTrack.id1 - regularTrack.id0;
            GraphNode n1 = new GraphNode();
            GraphNode n0 = new GraphNode(n1, cost);

            registerGraphNode(regularTrack.id0, n0);
            registerGraphNode(regularTrack.id1, n1);
        }

        // add station nodes
        for (Station station : stations) {
            GraphNode stationNode = new GraphNode();
            registerGraphNode(station.getId(), stationNode);
            for (StationTrack track : station.getTracks()) {
                for (StationTrack.ConnectingNode inboundNode : track.getInboundNodes()) {
                    nodes.get(inboundNode.getId()).add(stationNode, 0);
                }
                for (StationTrack.ConnectingNode outboundNode : track.getOutboundNodes()) {
                    stationNode.add(nodes.get(outboundNode.getId()), 0);
                }
            }
        }
    }

    private void registerGraphNode(int id, GraphNode node) {
        registerGraphNode(String.valueOf(id), node);
    }

    private void registerGraphNode(String id, GraphNode node) {
        if (nodes.containsKey(id)) {
            throw new IllegalArgumentException("Duplicated GraphNode id: " + id);
        }
        nodes.put(id, node);
    }

}
