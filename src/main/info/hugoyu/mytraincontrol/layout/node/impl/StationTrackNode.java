package info.hugoyu.mytraincontrol.layout.node.impl;

import com.google.common.collect.Range;
import info.hugoyu.mytraincontrol.exception.NodeAllocationException;
import info.hugoyu.mytraincontrol.trainset.Trainset;
import lombok.Getter;

import java.util.Map;

@Getter
public class StationTrackNode extends RegularTrackNode {

    private long id1;
    private String name;
    private int platformLength;
    private boolean isPlatformTrack, isPassingTrack;

    public StationTrackNode(long id0, long id1, String name, int trackLength, Map<Integer, Integer> sensors,
                            int platformLength, boolean isPlatformTrack, boolean isPassingTrack) {
        super(id0, isPassingTrack ? id1 : null, trackLength, sensors);

        this.id1 = id1;
        this.name = name;
        this.platformLength = platformLength;
        this.isPlatformTrack = isPlatformTrack;
        this.isPassingTrack = isPassingTrack;
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

        Range<Integer> allocatingRange = Range.closedOpen(0, (int) getInboundMoveDist(trainset));
        if (!isFree(trainset, allocatingRange)) {
            return false;
        }

        try {
            // allocate the centering section
            alloc(trainset, (int) getInboundMoveDist(trainset), null, null);
            free(trainset, (int) getInboundMargin(trainset));

            trainset.addAllocatedNode(this.id);
            return true;
        } catch (NodeAllocationException e) {
            // todo: log error
            return false;
        }
    }

    /**
     * non-blocking call, may return inaccurate result
     * @return whether the current track is free
     */
    public boolean isFree() {
        return super.owners.entrySet().stream()
                .allMatch(entry -> entry.getValue().isEmpty());
    }

    /**
     * @param trainset
     * @return distance to move so that the train is at the center of the track section
     */
    public float getInboundMoveDist(Trainset trainset) {
        // divide the entire track section into margin, trainLength, margin
        float trainLength = trainset.getTotalLength();
        return trainLength + getInboundMargin(trainset);
    }

    /**
     * @param trainset
     * @return length of the one-side margin for a train to be at the center of the track section
     */
    private float getInboundMargin(Trainset trainset) {
        int trainLength = trainset.getTotalLength();
        return (super.length - trainLength) / 2f;
    }

}
