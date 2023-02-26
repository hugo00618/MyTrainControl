package info.hugoyu.mytraincontrol.json.layout;

import info.hugoyu.mytraincontrol.layout.Vector;
import info.hugoyu.mytraincontrol.layout.alias.Station;
import info.hugoyu.mytraincontrol.layout.node.SensorAttachable;
import info.hugoyu.mytraincontrol.layout.node.impl.CrossoverNode;
import info.hugoyu.mytraincontrol.layout.node.impl.RegularTrackNode;
import info.hugoyu.mytraincontrol.layout.node.impl.StationTrackNode;
import info.hugoyu.mytraincontrol.layout.node.impl.TurnoutNode;
import info.hugoyu.mytraincontrol.registry.LayoutRegistry;
import info.hugoyu.mytraincontrol.registry.SensorRegistry;
import info.hugoyu.mytraincontrol.registry.SwitchableRegistry;
import info.hugoyu.mytraincontrol.sensor.SensorChangeListener;
import info.hugoyu.mytraincontrol.sensor.SensorState;
import info.hugoyu.mytraincontrol.switchable.AbstractSwitchable;
import info.hugoyu.mytraincontrol.trainset.Trainset;
import info.hugoyu.mytraincontrol.util.SensorUtil;
import jmri.Sensor;

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
                turnoutJson -> registerTurnout(turnoutJson, layoutRegistry, true));
        layoutJson.getDownlinkTurnouts().forEach(
                turnoutJson -> registerTurnout(turnoutJson, layoutRegistry, false));

        // crossovers
        layoutJson.getCrossovers().forEach(
                crossoverJson -> registerCrossover(crossoverJson, layoutRegistry));

        // sensors
        layoutJson.getSensors().forEach(
                sensorJson -> registerSensor(sensorJson)
        );
    }

    private static List<StationTrackNode> registerStationTracks(StationJson stationJson, LayoutRegistry layoutRegistry) {
        Stream<StationTrackNode> uplinkTrackNodes = registerStationTrackNodes(
                stationJson.getUplinkTracks(),
                layoutRegistry,
                true),
                downlinkTrackNodes = registerStationTrackNodes(
                        stationJson.getDownlinkTracks(),
                        layoutRegistry,
                        false);
        return Stream.concat(uplinkTrackNodes, downlinkTrackNodes)
                .collect(Collectors.toList());
    }

    private static Stream<StationTrackNode> registerStationTrackNodes(List<StationTrackJson> stationTrackJsons,
                                                                      LayoutRegistry layoutRegistry,
                                                                      boolean isUplink) {
        return stationTrackJsons.stream()
                .map(stationTrackJson -> {
                    StationTrackNode stationTrackNode = new StationTrackNode(stationTrackJson, isUplink);
                    layoutRegistry.registerGraphNode(stationTrackNode);
                    return stationTrackNode;
                });
    }

    private static void registerTurnout(TurnoutJson turnoutJson,
                                        LayoutRegistry layoutRegistry,
                                        boolean isUplink) {
        AbstractSwitchable turnout = SwitchableRegistry.getInstance()
                .registerSwitchable(turnoutJson.getAddress(), AbstractSwitchable.Type.TURNOUT);
        layoutRegistry.registerGraphNode(new TurnoutNode(turnoutJson, isUplink, turnout));
    }

    private static void registerCrossover(CrossoverJson crossoverJson, LayoutRegistry layoutRegistry) {
        AbstractSwitchable crossover = SwitchableRegistry.getInstance()
                .registerSwitchable(crossoverJson.getAddress(), AbstractSwitchable.Type.CROSSOVER);
        layoutRegistry.registerGraphNode(new CrossoverNode(crossoverJson, crossover));
    }

    private static void registerSensor(SensorJson sensorJson) {
        int address = sensorJson.getAddress();

        Vector nodeVector = new Vector(sensorJson.getNode());
        SensorAttachable node = (SensorAttachable) LayoutRegistry.getInstance().getNode(nodeVector);

        Sensor sensor = SensorUtil.getSensor(address, new SensorChangeListener() {
            @Override
            public void onEnter(Sensor sensor) {
                calibrateOwnerMovingBlockManager(SensorState.ENTER);
            }

            @Override
            public void onExit(Sensor sensor) {
                calibrateOwnerMovingBlockManager(SensorState.EXIT);
            }

            private void calibrateOwnerMovingBlockManager(SensorState sensorState) {
                Trainset occupyingTrainset = node.getOccupier(sensorJson.getOffset());
                if (occupyingTrainset != null) {
                    occupyingTrainset.calibrate(nodeVector, sensorJson.getOffset(), sensorState);
                }
            }
        });

        SensorRegistry.getInstance().registerSensor(sensor, nodeVector);
    }
}
