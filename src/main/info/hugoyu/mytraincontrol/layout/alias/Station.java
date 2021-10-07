package info.hugoyu.mytraincontrol.layout.alias;

import info.hugoyu.mytraincontrol.exception.InvalidIdException;
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

    /**
     * Finds the station track that is most likely to be available
     *
     * @param isPassingTrackRequired
     * @return
     */
    public StationTrackNode findAvailableTrack(long entryNodeId, boolean isPassingTrackRequired) {
        if (!entryNodeIds.contains(entryNodeId)) {
            throw new InvalidIdException(entryNodeId);
        }

        if (isPassingTrackRequired) {
            return stationTrackNodes.stream()
                    .filter(stationTrackNode ->
                            stationTrackNode.isPassingTrack() &&
                                    LayoutUtil.isReachable(entryNodeId, stationTrackNode.getId()))
                    // no sorting required since an entry node should map to only one passing track
                    .findFirst()
                    .orElseThrow(() ->
                            new InvalidIdException(entryNodeId)
                    );
        } else {
            return stationTrackNodes.stream()
                    .filter(stationTrackNode -> LayoutUtil.isReachable(entryNodeId, stationTrackNode.getId()))
                    // sort the stream to
                    // a. favour available tracks over non-available tracks
                    // b. favour non-passing track over passing tracks, if their availabilities are the same
                    .min((o1, o2) -> {
                        int free = Boolean.compare(o2.isFree(), o1.isFree());
                        if (free != 0) {
                            return free;
                        }
                        return Boolean.compare(o1.isPassingTrack(), o2.isPassingTrack());
                    })
                    .orElseThrow(() ->
                            new InvalidIdException(entryNodeId)
                    );
        }
    }
}
