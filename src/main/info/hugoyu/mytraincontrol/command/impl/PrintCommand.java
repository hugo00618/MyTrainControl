package info.hugoyu.mytraincontrol.command.impl;

import info.hugoyu.mytraincontrol.command.Command;
import info.hugoyu.mytraincontrol.util.LayoutUtil;
import info.hugoyu.mytraincontrol.util.TrainUtil;

public class PrintCommand implements Command {

    private static final String LIST_TYPE_TRAINS = "trains";
    private static final String LIST_TYPE_STATIONS = "stations";

    private enum ListType {
        TRAINS,
        STATIONS
    }

    @Override
    public void execute(String[] args) {
        String typeStr = args[1].toUpperCase();
        ListType type = ListType.valueOf(typeStr);
        switch (type) {
            case TRAINS:
                printTrains();
                break;
            case STATIONS:
                printStations();
                break;
            default:
                // should not run to here
                throw new RuntimeException();
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
            trainset.getAllocatedNodesSummary()
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
                    System.out.println("\t\t" + stationTrackNode.getId0()));
        });
        System.out.println();
    }
}
