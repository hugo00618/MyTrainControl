package info.hugoyu.mytraincontrol.layout.node.impl;

import com.google.common.collect.Range;
import info.hugoyu.mytraincontrol.json.layout.StationTrackJson;
import info.hugoyu.mytraincontrol.layout.Vector;
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
    public void removeOccupier(Vector vector, Trainset trainset) {
        super.removeOccupier(vector, trainset);

        if (station != null) {
            station.broadcast();
        }
    }

    /**
     * @param trainset
     * @return false if this is not a platform track or the trainset is longer than the track length, true otherwise
     */
    public boolean isPlatformTrackAbleToFit(Trainset trainset) {
        return isPlatformTrack && trainset.getTotalLength() <= length;
    }

    @Override
    public boolean isFree(Trainset trainset, Vector vector, Range<Integer> range) {
        synchronized (occupierLock) {
            // return true if there is no occupier or if the current occupier is trainset itself
            return occupiers.isEmpty() || occupiers.keySet().iterator().next().equals(trainset.getAddress());
        }
    }

    public boolean isFree() {
        synchronized (occupierLock) {
            return occupiers.isEmpty();
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
    public int getInboundMargin(Trainset trainset) {
        int trainLength = trainset.getTotalLength();
        return (length - trainLength) / 2;
    }

}
