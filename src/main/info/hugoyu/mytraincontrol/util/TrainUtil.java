package info.hugoyu.mytraincontrol.util;

import info.hugoyu.mytraincontrol.json.MyJsonReader;
import info.hugoyu.mytraincontrol.layout.Route;
import info.hugoyu.mytraincontrol.layout.node.AbstractTrackNode;
import info.hugoyu.mytraincontrol.layout.node.impl.StationTrackNode;
import info.hugoyu.mytraincontrol.registry.ThrottleRegistry;
import info.hugoyu.mytraincontrol.registry.TrainsetRegistry;
import info.hugoyu.mytraincontrol.trainset.Trainset;
import info.hugoyu.mytraincontrol.trainset.TrainsetProfile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class TrainUtil {

    private static final String TRAINSET_PROFILE_DIR = "trainset-profiles";

    public static void moveDist(int address, int dist) {
        Trainset trainset = getTrainset(address);
        trainset.move(dist);
    }

    public static void registerTrainset(int address, String name, String profileFilename) throws Exception {
        TrainsetRegistry.getInstance().registerTrainset(address, name, profileFilename);
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
            return MyJsonReader.parseJSON(TRAINSET_PROFILE_DIR + "/" + fileName, TrainsetProfile.class);
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

    public static boolean allocateStationTrackImmediate(int address, long trackNodeId) {
        Trainset trainset = getTrainset(address);

        AbstractTrackNode node = LayoutUtil.getNode(trackNodeId);
        if (!(node instanceof StationTrackNode)) {
            throw new RuntimeException("Node id does not map to a station track node: " + trackNodeId);
        }

        StationTrackNode stationTrackNode = (StationTrackNode) node;
        return stationTrackNode.reserve(trainset);
    }

    public static void moveTo(int address, String stationId) {
        Trainset trainset = getTrainset(address);

        List<Long> allocatedNodes = trainset.getAllocatedNodes();
        if (allocatedNodes.isEmpty()) {
            throw new RuntimeException("Trainset does not own any node");
        }
        long fromNodeId = allocatedNodes.get(allocatedNodes.size() - 1);
        Route route = RouteUtil.findRouteToStation(fromNodeId, stationId);
        if (route == null) {
            throw new RuntimeException("No route to station");
        }

        trainset.move(route);
    }
}
