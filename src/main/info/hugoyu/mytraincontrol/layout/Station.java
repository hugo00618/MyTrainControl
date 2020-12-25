package info.hugoyu.mytraincontrol.layout;

import lombok.Getter;

import java.util.List;

@Getter
public class Station {

    private String name;
    private String id;
    private List<StationTrack> tracks;

}
