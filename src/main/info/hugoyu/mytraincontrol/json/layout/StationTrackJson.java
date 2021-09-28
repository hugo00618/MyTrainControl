package info.hugoyu.mytraincontrol.json.layout;

import lombok.Getter;

@Getter
public class StationTrackJson extends RegularTrackJson {
    private String name;
    private boolean isPlatformTrack, isPassingTrack;
    private int trackLength, platformLength;
}
