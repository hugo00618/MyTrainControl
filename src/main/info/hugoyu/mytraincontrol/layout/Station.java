package info.hugoyu.mytraincontrol.layout;

import lombok.Getter;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
public class Station {

    private String name;
    private List<StationTrack> tracks;

    private Set<StationTrack.ConnectingNode> inboundNodes;

    public Set<StationTrack.ConnectingNode> getInboundNodes() {
        if (inboundNodes == null) {
            inboundNodes = tracks.stream()
                    .map(track -> track.getInboundNode())
                    .collect(Collectors.toSet());
        }
        return inboundNodes;
    }

    public StationTrack getTrack(StationTrack.ConnectingNode inboundNode) {
        return tracks.stream()
                .filter(track -> track.getInboundNode().equals(inboundNode))
                .findFirst()
                .orElse(null);
    }

}
