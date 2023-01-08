package info.hugoyu.mytraincontrol.layout.node.impl;

import com.google.common.collect.Range;
import info.hugoyu.mytraincontrol.exception.NodeAllocationException;
import info.hugoyu.mytraincontrol.json.layout.StationTrackJson;
import info.hugoyu.mytraincontrol.layout.BlockSectionResult;
import info.hugoyu.mytraincontrol.layout.alias.Station;
import info.hugoyu.mytraincontrol.trainset.Trainset;
import lombok.Getter;
import lombok.Setter;

public class StationTrackNode extends RegularTrackNode {

    @Getter
    private String name;

    @Getter
    private int trackLength;

    @Getter
    private boolean isPlatformTrack;

    @Getter
    private boolean isPassingTrack;

    @Setter
    private Station station;

    public StationTrackNode(long id0, long id1, String name, int trackLength, boolean isUplink,
                            boolean isPlatformTrack, boolean isPassingTrack) {
        super(id0, id1, trackLength, isUplink, true);

        this.name = name;
        this.trackLength = trackLength;
        this.isPlatformTrack = isPlatformTrack;
        this.isPassingTrack = isPassingTrack;
    }

    public StationTrackNode(StationTrackJson stationTrackJson, boolean isUplink) {
        this(stationTrackJson.getId0(),
                stationTrackJson.getId1(),
                stationTrackJson.getName(),
                stationTrackJson.getTrackLength(),
                isUplink,
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
     * @param trainset
     * @return false if this is not a platform track or the trainset is longer than the track length, true otherwise
     */
    public boolean isPlatformTrackAbleToFit(Trainset trainset) {
        return isPlatformTrack && trainset.getTotalLength() <= length;
    }

    public boolean reserve(Trainset trainset) {
        if (!isPlatformTrackAbleToFit(trainset)) {
            return false;
        }

        synchronized (ownersLock) {
            Range<Integer> entireSection = Range.closedOpen(0, length);
            if (!isFree(trainset, entireSection)) {
                return false;
            }

            try {
                trainset.freeAllNodes();

                // allocate the centering section
                alloc(trainset, getInboundMoveDist(trainset), null, null);
                free(trainset, getInboundMargin(trainset));

                trainset.addAllocatedNode(this.id0);
                return true;
            } catch (NodeAllocationException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public boolean isFree() {
        synchronized (ownersLock) {
            return owners.isEmpty();
        }
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
