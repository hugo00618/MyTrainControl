package info.hugoyu.mytraincontrol.json.layout;

import info.hugoyu.mytraincontrol.layout.alias.Station;
import info.hugoyu.mytraincontrol.layout.node.impl.RegularTrackNode;
import info.hugoyu.mytraincontrol.layout.node.impl.StationTrackNode;
import info.hugoyu.mytraincontrol.layout.node.impl.TurnoutNode;
import info.hugoyu.mytraincontrol.registry.LayoutRegistry;

import java.util.List;
import java.util.stream.Collectors;

public class LayoutProvider {

    public static void registerLayout(LayoutJson layoutJson) {
        LayoutRegistry.getNewInstance();
        registerNodes(layoutJson);
    }

    private static void registerNodes(LayoutJson layoutJson) {
        LayoutRegistry layoutRegistry = LayoutRegistry.getInstance();

        // regular nodes
        layoutJson.getRegularTracks().forEach(
                regularTrackJson -> layoutRegistry.registerGraphNode(new RegularTrackNode(regularTrackJson)));

        // stations
        layoutJson.getStations().forEach(
                stationJson -> {
                    List<StationTrackNode> stationTrackNodes = registerStationTracks(stationJson, layoutRegistry);
                    layoutRegistry.registerAlias(new Station(stationJson, stationTrackNodes));
                });

        // turnouts
        layoutJson.getTurnouts().forEach(
                turnoutJson -> layoutRegistry.registerGraphNode(new TurnoutNode(turnoutJson)));
    }

    private static List<StationTrackNode> registerStationTracks(StationJson stationJson, LayoutRegistry layoutRegistry) {
        return stationJson.getTracks().stream()
                .map(stationTrackJson -> {
                    StationTrackNode stationTrackNode = new StationTrackNode(stationTrackJson);
                    layoutRegistry.registerGraphNode(stationTrackNode);
                    return stationTrackNode;
                })
                .collect(Collectors.toList());
    }
}
