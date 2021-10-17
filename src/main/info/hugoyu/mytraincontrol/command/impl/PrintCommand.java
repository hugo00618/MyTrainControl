package info.hugoyu.mytraincontrol.command.impl;

import info.hugoyu.mytraincontrol.command.Command;
import info.hugoyu.mytraincontrol.util.LayoutUtil;
import info.hugoyu.mytraincontrol.util.TrainUtil;

import java.util.stream.Collectors;

public class PrintCommand implements Command {

    private static final String LIST_TYPE_TRAINS = "trains";
    private static final String LIST_TYPE_STATIONS = "stations";

    private String type;

    @Override
    public boolean parseArgs(String[] args) {
        String type = args[1];
        if (!type.equals(LIST_TYPE_TRAINS) && !type.equals(LIST_TYPE_STATIONS)) {
            return false;
        }

        this.type = type;
        return true;
    }

    @Override
    public void execute() {
        switch (type) {
            case LIST_TYPE_TRAINS:
                printTrains();
                break;
            case LIST_TYPE_STATIONS:
                printStations();
                break;
            default:
                break;
        }
    }

    @Override
    public String[] expectedArgs() {
        return new String[]{"trains/stations"};
    }

    private void printTrains() {
        System.out.println("Registered trains:");
        TrainUtil.getTrainsets().forEach((address, trainset) -> {
            System.out.println(String.format("%d: %s", address, trainset.getName()));

            System.out.println("\tOwned sections:");
            trainset.getAllocatedNodes().stream()
                    .collect(Collectors.toMap(
                            nodeId -> nodeId,
                            nodeId -> LayoutUtil.getNode(nodeId).getOwnerStatus(address)
                    ))
                    .forEach((nodeId, ownerDetails) ->
                            System.out.println(String.format("\t\t%d: %s", nodeId, ownerDetails)));
        });
        System.out.println();
    }

    private void printStations() {
        System.out.println("Stations:");
        LayoutUtil.getStations().values().forEach(station -> {
            System.out.println(station.getName());

            System.out.println("\tTracks:");
            station.getStationTrackNodes().forEach(stationTrackNode ->
                    System.out.println("\t\t" + stationTrackNode.getId()));
        });
        System.out.println();
    }
}
