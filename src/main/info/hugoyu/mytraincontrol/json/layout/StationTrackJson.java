package info.hugoyu.mytraincontrol.json.layout;

import lombok.Getter;

@Getter
public class StationTrackJson extends TrackJson {
    private String name;
    private boolean isPlatformTrack, isPassingTrack;
    private int trackLength, platformLength;
}
