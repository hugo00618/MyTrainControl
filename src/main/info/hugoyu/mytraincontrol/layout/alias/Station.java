package info.hugoyu.mytraincontrol.layout.alias;

import info.hugoyu.mytraincontrol.layout.node.track.impl.StationTrackNode;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class Station {
    private String id;
    private String name;
    private List<StationTrackNode> stationTackNodes;
    private List<Long> entryNodeIds;

    public StationTrackNode getFreeTrack(boolean isPassing) {
        if (isPassing) {
            return stationTackNodes.stream()
                    .filter(stationTrackNode -> stationTrackNode.isPassingTrack() && stationTrackNode.isFree())
                    .findFirst().get();
        } else {
            return stationTackNodes.stream()
                    .filter(stationTrackNode -> stationTrackNode.isFree())
                    // sort the stream to put non-passing tracks at front
                    .sorted((o1, o2) -> Boolean.compare(o1.isPassingTrack(), o2.isPassingTrack()))
                    .findFirst().get();
        }
    }
}
