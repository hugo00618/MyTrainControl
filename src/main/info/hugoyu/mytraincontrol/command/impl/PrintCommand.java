package info.hugoyu.mytraincontrol.command.impl;

import info.hugoyu.mytraincontrol.command.Command;
import info.hugoyu.mytraincontrol.exception.CommandInvalidUsageException;
import info.hugoyu.mytraincontrol.util.LayoutUtil;
import info.hugoyu.mytraincontrol.util.TrainUtil;

import java.util.stream.Collectors;

public class PrintCommand implements Command {

    private static final String LIST_TYPE_TRAINS = "trains";
    private static final String LIST_TYPE_STATIONS = "stations";

    @Override
    public void execute(String[] args) throws Exception {
        String type = args[1];
        switch (type) {
            case LIST_TYPE_TRAINS:
                printTrains();
                break;
            case LIST_TYPE_STATIONS:
                printStations();
                break;
            default:
                throw new CommandInvalidUsageException(this);
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
            station.getStationTackNodes().forEach(stationTrackNode ->
                    System.out.println("\t\t" + stationTrackNode.getId()));
        });
        System.out.println();
    }
}
