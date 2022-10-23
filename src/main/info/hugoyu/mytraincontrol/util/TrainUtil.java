package info.hugoyu.mytraincontrol.util;

import info.hugoyu.mytraincontrol.json.MyJsonReader;
import info.hugoyu.mytraincontrol.layout.Route;
import info.hugoyu.mytraincontrol.layout.node.impl.StationTrackNode;
import info.hugoyu.mytraincontrol.registry.ThrottleRegistry;
import info.hugoyu.mytraincontrol.registry.TrainsetRegistry;
import info.hugoyu.mytraincontrol.trainset.Trainset;
import info.hugoyu.mytraincontrol.trainset.TrainsetProfile;

import java.io.IOException;
import java.util.Map;

public class TrainUtil {

    private static final String TRAINSET_PROFILE_DIR = "trainset-profiles";

    public static void moveDist(int address, int dist) {
        Trainset trainset = getTrainset(address);
        if (dist > 0) {
            trainset.setIsForward(true);
        } else {
            trainset.setIsForward(false);
        }

        trainset.setDistToMove(Math.abs(dist));
    }

    public static void registerTrainset(int address, String name, String profileFilename, boolean isMotorReversed) {
        TrainsetRegistry.getInstance().registerTrainset(address, new Trainset(address, name, profileFilename, isMotorReversed));
        ThrottleRegistry.getInstance().registerThrottle(address);
    }

    public static Trainset getTrainset(int address) {
        Trainset trainset = TrainsetRegistry.getInstance().getTrainset(address);
        if (trainset == null) {
            throw new RuntimeException("No trainset is not registered at address: " + address);
        }
        return trainset;
    }

    public static TrainsetProfile getTrainsetProfile(String fileName) {
        try {
            TrainsetProfile trainsetProfile = MyJsonReader.parseJSON(TRAINSET_PROFILE_DIR + "/" + fileName, TrainsetProfile.class);
            trainsetProfile.postDeserialization();
            return trainsetProfile;
        } catch (IOException e) {
            throw new RuntimeException("Error parsing trainset profile: " + fileName);
        }
    }

    public static Map<Integer, Trainset> getTrainsets() {
        return TrainsetRegistry.getInstance().getTrainsets();
    }

    public static void setLight(int address, boolean on) {
        Trainset trainset = getTrainset(address);
        trainset.setIsLightOn(on);
    }

    public static boolean allocateStationTrackImmediately(int address, long trackNodeId) {
        Trainset trainset = getTrainset(address);
        StationTrackNode stationTrackNode = LayoutUtil.getStationTrackNode(trackNodeId);
        return stationTrackNode.reserve(trainset);
    }

    public static void freeAllAllocatedNodes(int address) {
        Trainset trainset = getTrainset(address);
        trainset.freeAllNodes();
    }

    public static void moveTo(int address, String stationId) {
        Trainset trainset = getTrainset(address);

        Long fromNodeId = trainset.getLastAllocatedNode();
        if (fromNodeId == null) {
            throw new RuntimeException(String.format("Trainset %s does not own any node", trainset.getName()));
        }

        Route route = RouteUtil.findRouteToStation(fromNodeId, stationId);
        if (route == null) {
            throw new RuntimeException("No route to station");
        }
        if (route.getNodes().size() == 1) {
            throw new RuntimeException("Train is already at station: " + stationId);
        }

        trainset.move(route);
    }
}
