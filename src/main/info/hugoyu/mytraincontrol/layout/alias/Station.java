package info.hugoyu.mytraincontrol.layout.alias;

import info.hugoyu.mytraincontrol.exception.InvalidIdException;
import info.hugoyu.mytraincontrol.json.layout.StationJson;
import info.hugoyu.mytraincontrol.layout.node.impl.StationTrackNode;
import info.hugoyu.mytraincontrol.util.LayoutUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class Station {
    private String id;
    private String name;
    private List<StationTrackNode> stationTrackNodes;
    private List<Long> entryNodeIds;

    public Station(StationJson stationJson, List<StationTrackNode> stationTrackNodes) {
        this(stationJson.getId(),
                stationJson.getName(),
                stationTrackNodes,
                stationJson.getEntryNodeIds());
    }

    /**
     * Finds the station track that is currently available
     *
     * @param isPassingTrackRequired
     * @return
     */
    // todo: change this to return route directly
    public StationTrackNode findAvailableTrack(long entryNodeId, boolean isPassingTrackRequired) {
        if (!entryNodeIds.contains(entryNodeId)) {
            throw new InvalidIdException(entryNodeId);
        }

        if (isPassingTrackRequired) {
            return stationTrackNodes.stream()
                    .filter(stationTrackNode ->
                            stationTrackNode.isPassingTrack() &&
                                    stationTrackNode.isFree() &&
                                    LayoutUtil.isReachable(entryNodeId, stationTrackNode.getId()))
                    .findFirst()
                    .orElse(null);
        } else {
            return stationTrackNodes.stream()
                    .filter(stationTrackNode -> stationTrackNode.isFree() &&
                            LayoutUtil.isReachable(entryNodeId, stationTrackNode.getId()))
                    // sort the stream to favour non-passing track over passing tracks
                    .min((o1, o2) -> Boolean.compare(o1.isPassingTrack(), o2.isPassingTrack()))
                    .orElse(null);
        }
    }
}
