package info.hugoyu.mytraincontrol.json.layout;

import lombok.Getter;

import java.util.List;

@Getter
public class StationJson {
    private String id;
    private String name;
    private List<Long> entryNodeIds;
    private List<StationTrackJson> tracks;
}
