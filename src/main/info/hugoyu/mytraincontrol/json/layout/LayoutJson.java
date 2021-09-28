package info.hugoyu.mytraincontrol.json.layout;

import lombok.Getter;

import java.util.List;

@Getter
public class LayoutJson {
    private List<RegularTrackJson> regularTracks;
    private List<TurnoutJson> turnouts;
    private List<StationJson> stations;
}
