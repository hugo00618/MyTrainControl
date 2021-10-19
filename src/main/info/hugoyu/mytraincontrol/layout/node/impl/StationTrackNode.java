package info.hugoyu.mytraincontrol.layout.node.impl;

import com.google.common.collect.Range;
import info.hugoyu.mytraincontrol.exception.NodeAllocationException;
import info.hugoyu.mytraincontrol.json.layout.StationTrackJson;
import info.hugoyu.mytraincontrol.layout.BlockSectionResult;
import info.hugoyu.mytraincontrol.layout.alias.Station;
import info.hugoyu.mytraincontrol.trainset.Trainset;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
public class StationTrackNode extends RegularTrackNode {

    private long id1;
    private String name;
    private int platformLength;
    private boolean isPlatformTrack, isPassingTrack;

    @Setter
    private Station station;

    public StationTrackNode(long id0, long id1, String name, int trackLength, boolean isUplink, Map<Integer, Integer> sensors,
                            int platformLength, boolean isPlatformTrack, boolean isPassingTrack) {
        super(id0, id1, trackLength, isUplink, sensors);

        this.id1 = id1;
        this.name = name;
        this.platformLength = platformLength;
        this.isPlatformTrack = isPlatformTrack;
        this.isPassingTrack = isPassingTrack;
    }

    public StationTrackNode(StationTrackJson stationTrackJson, boolean isUplink) {
        this(stationTrackJson.getId0(),
                stationTrackJson.getId1(),
                stationTrackJson.getName(),
                stationTrackJson.getTrackLength(),
                isUplink,
                stationTrackJson.getSensors(),
                stationTrackJson.getPlatformLength(),
                stationTrackJson.isPlatformTrack(),
                stationTrackJson.isPassingTrack());
    }

    @Override
    public BlockSectionResult free(Trainset trainset, int dist) throws NodeAllocationException {
        BlockSectionResult blockSectionResult = super.free(trainset, dist);

        if (blockSectionResult.isEntireSectionConsumed() && station != null) {
            station.broadcast();
        }

        return blockSectionResult;
    }

    /**
     * Only called by TrainUtil when initializing a trainset on a certain station track
     *
     * @param trainset
     * @return whether reserve() is successful
     */
    public boolean reserve(Trainset trainset) {
        boolean isTrainsetAbleToFit = trainset.getTotalLength() <= platformLength;
        if (!isPlatformTrack || !isTrainsetAbleToFit) {
            // todo: log error
            return false;
        }

        Range<Integer> allocatingRange = Range.closedOpen(0, getInboundMoveDist(trainset));
        if (!isFree(trainset, allocatingRange)) {
            return false;
        }

        try {
            // allocate the centering section
            alloc(trainset, getInboundMoveDist(trainset), null, null);
            free(trainset, getInboundMargin(trainset));

            trainset.addAllocatedNode(this.id);
            return true;
        } catch (NodeAllocationException e) {
            // todo: log error
            return false;
        }
    }

    /**
     * non-blocking call, may return inaccurate result
     *
     * @return whether the current track is free
     */
    public boolean isFree() {
        return owners.isEmpty();
    }

    /**
     * @param trainset
     * @return distance to move so that the train is at the center of the track section
     */
    public int getInboundMoveDist(Trainset trainset) {
        // divide the entire track section into margin, trainLength, margin
        int trainLength = trainset.getTotalLength();
        return trainLength + getInboundMargin(trainset);
    }

    /**
     * @param trainset
     * @return distance to move the train out of the section
     */
    public int getOutboundMoveDist(Trainset trainset) {
        return length - getInboundMoveDist(trainset);
    }

    /**
     * @param trainset
     * @return length of the one-side margin for a train to be at the center of the track section
     */
    private int getInboundMargin(Trainset trainset) {
        int trainLength = trainset.getTotalLength();
        return (length - trainLength) / 2;
    }

}
