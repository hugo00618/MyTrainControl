package info.hugoyu.mytraincontrol.json.layout;

import info.hugoyu.mytraincontrol.layout.alias.Station;
import info.hugoyu.mytraincontrol.layout.node.impl.RegularTrackNode;
import info.hugoyu.mytraincontrol.layout.node.impl.StationTrackNode;
import info.hugoyu.mytraincontrol.layout.node.impl.TurnoutNode;
import info.hugoyu.mytraincontrol.registry.LayoutRegistry;
import info.hugoyu.mytraincontrol.registry.TurnoutRegistry;
import info.hugoyu.mytraincontrol.turnout.Turnout;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LayoutProvider {

    public static void registerLayout(LayoutJson layoutJson) {
        LayoutRegistry.getNewInstance();
        registerNodes(layoutJson);
    }

    private static void registerNodes(LayoutJson layoutJson) {
        LayoutRegistry layoutRegistry = LayoutRegistry.getInstance();

        // regular nodes
        layoutJson.getUplinkRegularTracks().forEach(
                regularTrackJson -> layoutRegistry.registerGraphNode(new RegularTrackNode(regularTrackJson, true)));
        layoutJson.getDownlinkRegularTracks().forEach(
                regularTrackJson -> layoutRegistry.registerGraphNode(new RegularTrackNode(regularTrackJson, false)));

        // stations
        layoutJson.getStations().forEach(
                stationJson -> {
                    List<StationTrackNode> stationTrackNodes = registerStationTracks(stationJson, layoutRegistry);
                    layoutRegistry.registerAlias(new Station(stationJson, stationTrackNodes));
                });

        // turnouts
        layoutJson.getUplinkTurnouts().forEach(
                turnoutJson -> registerTurnout(turnoutJson, true));
        layoutJson.getDownlinkTurnouts().forEach(
                turnoutJson -> registerTurnout(turnoutJson, false));
    }

    private static List<StationTrackNode> registerStationTracks(StationJson stationJson, LayoutRegistry layoutRegistry) {
        Stream<StationTrackNode> uplinkTrackNodes = registerStationTrackNodes(stationJson.getUplinkTracks(), layoutRegistry, true),
                downlinkTrackNodes = registerStationTrackNodes(stationJson.getDownlinkTracks(), layoutRegistry, false);
        return Stream.concat(uplinkTrackNodes, downlinkTrackNodes)
                .collect(Collectors.toList());
    }

    private static Stream<StationTrackNode> registerStationTrackNodes(List<StationTrackJson> stationTrackJsons,
                                                                      LayoutRegistry layoutRegistry, boolean isUplink) {
        return stationTrackJsons.stream()
                .map(stationTrackJson -> {
                    StationTrackNode stationTrackNode = new StationTrackNode(stationTrackJson, isUplink);
                    layoutRegistry.registerGraphNode(stationTrackNode);
                    return stationTrackNode;
                });
    }

    private static void registerTurnout(TurnoutJson turnoutJson, boolean isUplink) {
        Turnout turnout = TurnoutRegistry.getInstance().registerTurnout(turnoutJson);
        LayoutRegistry.getInstance().registerGraphNode(new TurnoutNode(turnoutJson, isUplink, turnout));
    }
}
