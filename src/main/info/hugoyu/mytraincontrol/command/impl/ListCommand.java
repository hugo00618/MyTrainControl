package info.hugoyu.mytraincontrol.command.impl;

import info.hugoyu.mytraincontrol.command.ICommand;
import info.hugoyu.mytraincontrol.exception.CommandInvalidUsageException;
import info.hugoyu.mytraincontrol.layout.Station;
import info.hugoyu.mytraincontrol.layout.StationTrack;
import info.hugoyu.mytraincontrol.registry.TrainsetRegistry;
import info.hugoyu.mytraincontrol.trainset.Trainset;
import info.hugoyu.mytraincontrol.util.LayoutUtil;

import java.util.Collection;
import java.util.Map;

public class ListCommand implements ICommand {

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
    public String argList() {
        return "{trains/stations}";
    }

    @Override
    public int numberOfArgs() {
        return 2;
    }

    private void printTrains() {
        Map<Integer, Trainset> trainsets = TrainsetRegistry.getInstance().getTrainsets();
        System.out.println("Registered trains:");
        for (Map.Entry<Integer, Trainset> trainsetEntry : trainsets.entrySet()) {
            Trainset trainset = trainsetEntry.getValue();
            System.out.println(String.format("%d %s", trainsetEntry.getKey(), trainset.getName()));
        }
        System.out.println();
    }

    private void printStations() {
        Collection<Station> stations = LayoutUtil.getStations().values();
        System.out.println("Stations:");
        for (Station station : stations) {
            System.out.println(station.getName());

            System.out.println("\tTracks:");
            for (StationTrack track : station.getTracks()) {
                System.out.println("\t\t" + track.getId());
            }
        }
        System.out.println();
    }
}
