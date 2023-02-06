package info.hugoyu.mytraincontrol.util;

import info.hugoyu.mytraincontrol.command.impl.AllocateCommand;
import info.hugoyu.mytraincontrol.exception.InvalidIdException;
import info.hugoyu.mytraincontrol.json.SavedLayoutState;
import info.hugoyu.mytraincontrol.json.layout.LayoutJson;
import info.hugoyu.mytraincontrol.json.layout.LayoutProvider;
import info.hugoyu.mytraincontrol.layout.Vector;
import info.hugoyu.mytraincontrol.layout.alias.Station;
import info.hugoyu.mytraincontrol.layout.node.AbstractTrackNode;
import info.hugoyu.mytraincontrol.layout.node.impl.StationTrackNode;
import info.hugoyu.mytraincontrol.registry.LayoutRegistry;
import info.hugoyu.mytraincontrol.registry.TrainsetRegistry;
import lombok.extern.log4j.Log4j;

import java.util.AbstractMap;
import java.util.Map;
import java.util.stream.Collectors;

@Log4j
public class LayoutUtil {

    private static final String LAYOUT_FILE_PATH = "json-layout-profiles/layout.json";
    private static final String SAVED_LAYOUT_STATE_FILE_PATH = "saved_layout.json";

    public static void registerLayout() {
        registerLayout(LAYOUT_FILE_PATH);
    }

    public static void registerLayout(String filePath) {
        LayoutJson layoutJson = FileUtil.readJson(filePath, LayoutJson.class);
        LayoutProvider.registerLayout(layoutJson);
    }

    public static AbstractTrackNode getNode(Vector vector) {
        AbstractTrackNode node = LayoutRegistry.getInstance().getNode(vector);
        if (node == null) {
            throw new InvalidIdException(vector.toString(), InvalidIdException.Type.NOT_FOUND);
        }
        return node;
    }

    public static AbstractTrackNode getNode(long id0, long id1) {
        return getNode(new Vector(id0, id1));
    }

    public static StationTrackNode getStationTrackNode(Vector vector) {
        try {
            return (StationTrackNode) getNode(vector);
        } catch (ClassCastException e) {
            throw new InvalidIdException(vector.toString(), InvalidIdException.Type.NOT_FOUND);
        }
    }

    public static StationTrackNode getStationTrackNode(long id0, long id1) {
        return getStationTrackNode(new Vector(id0, id1));
    }

    public static Station getStation(String id) {
        Station station = LayoutRegistry.getInstance().getStation(id);
        if (station == null) {
            throw new InvalidIdException(id, InvalidIdException.Type.NOT_FOUND);
        }
        return station;
    }

    /**
     * Get station from entry node id
     *
     * @param entryNodeId
     * @return
     */
    public static Station getStation(long entryNodeId) {
        return LayoutRegistry.getInstance().getStations().values().stream()
                .filter(station ->
                        (station.getUplinkEntryNode() != null && station.getUplinkEntryNode().equals(entryNodeId)) ||
                                (station.getDownlinkEntryNode() != null && station.getDownlinkEntryNode().equals(entryNodeId)))
                .findFirst()
                .orElse(null);
    }

    public static Map<String, Station> getStations() {
        return LayoutRegistry.getInstance().getStations();
    }

    /**
     * Saves the current layout state into a file.
     * <p>
     * Layout state includes:
     * Occupation state for trains in the station
     */
    public static void saveLayoutState() {
        // Map<trainset address, occupied vector>
        Map<Integer, Vector> occupationState = TrainsetRegistry.getInstance().getTrainsets().values().stream()
                .flatMap(trainset -> trainset.getAllocatedStationTrack().stream()
                        .map(stationTrackNode -> new AbstractMap.SimpleEntry<>(
                                trainset.getAddress(),
                                new Vector(stationTrackNode.getId0(), stationTrackNode.getId1()))))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        SavedLayoutState savedLayoutState = SavedLayoutState.builder()
                .occupationState(occupationState)
                .build();

        FileUtil.writeJson(SAVED_LAYOUT_STATE_FILE_PATH, savedLayoutState);

        System.out.println("Current layout state has been stored");
    }

    public static void restoreLayoutState() {
        System.out.println("Restoring from saved layout state...");

        try {
            SavedLayoutState savedLayoutState = FileUtil.readJson(SAVED_LAYOUT_STATE_FILE_PATH, SavedLayoutState.class);

            // restore occupation state
            Map<Integer, Vector> occupationState = savedLayoutState.getOccupationState();
            if (occupationState != null) {
                occupationState.forEach((trainsetAddress, stationTrackVector) ->
                        CommandUtil.getCommand(AllocateCommand.class).execute(new String[]{
                                "", // command
                                String.valueOf(trainsetAddress),
                                String.valueOf(stationTrackVector.getId0()),
                                String.valueOf(stationTrackVector.getId1())
                        }));
            }

            System.out.println("Layout state restored");
        } catch (RuntimeException e) {
            System.out.println("Error reading saved layout file, nothing restored");
        }
    }

}
