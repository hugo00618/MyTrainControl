package info.hugoyu.mytraincontrol.json.layout;

import lombok.Getter;

import java.util.List;

@Getter
public class LayoutJson {
    private List<RegularTrackJson> uplinkRegularTracks, downlinkRegularTracks;
    private List<TurnoutJson> uplinkTurnouts, downlinkTurnouts;
    private List<CrossoverJson> crossovers;
    private List<SensorJson> sensors;
    private List<StationJson> stations;
}
