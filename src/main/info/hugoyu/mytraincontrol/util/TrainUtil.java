package info.hugoyu.mytraincontrol.util;

import info.hugoyu.mytraincontrol.layout.Route;
import info.hugoyu.mytraincontrol.layout.node.impl.StationTrackNode;
import info.hugoyu.mytraincontrol.registry.ThrottleRegistry;
import info.hugoyu.mytraincontrol.registry.TrainsetRegistry;
import info.hugoyu.mytraincontrol.trainset.Trainset;
import info.hugoyu.mytraincontrol.trainset.TrainsetProfile;
import jmri.DccThrottle;

import java.io.IOException;
import java.util.Map;

public class TrainUtil {

    private static final String TRAINSET_PROFILE_DIR = "trainset-profiles";

    public static void registerTrainset(int address, String name, String profileFilename, boolean isMotorReversed) {
        TrainsetRegistry.getInstance().registerTrainset(address, new Trainset(address, name, profileFilename, isMotorReversed));
        ThrottleRegistry.getInstance().registerThrottle(address);
    }

    public static Trainset getTrainset(int address) {
        return TrainsetRegistry.getInstance().getTrainset(address);
    }

    public static TrainsetProfile getTrainsetProfile(String fileName) {
        try {
            TrainsetProfile trainsetProfile = JsonUtil.parseJSON(TRAINSET_PROFILE_DIR + "/" + fileName, TrainsetProfile.class);
            trainsetProfile.postDeserialization();
            return trainsetProfile;
        } catch (IOException e) {
            throw new RuntimeException("Error parsing trainset profile: " + fileName);
        }
    }

    public static Map<Integer, Trainset> getTrainsets() {
        return TrainsetRegistry.getInstance().getTrainsets();
    }

    public static void setLight(Trainset trainset, LightState lightState) {
        trainset.setIsLightOn(lightState);
    }

    public static boolean allocateStationTrackImmediately(Trainset trainset, long trackNodeId) {
        StationTrackNode stationTrackNode = LayoutUtil.getStationTrackNode(trackNodeId);
        return stationTrackNode.reserve(trainset);
    }

    public static void freeAllAllocatedNodes(Trainset trainset) {
        trainset.freeAllNodes();
    }

    public static void moveDist(Trainset trainset, int dist) {
        boolean isForward = dist > 0;
        trainset.setIsForward(isForward);
        trainset.setDistToMove(Math.abs(dist));
    }

    public static void moveTo(Trainset trainset, String stationId) {
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

    public static void setThrottle(Trainset trainset, int throttlePercent) {
        boolean isForward = throttlePercent >= 0;
        DccThrottle throttle = getDccThrottle(trainset);
        throttle.setIsForward(isForward);
        throttle.setSpeedSetting((float) (Math.abs(throttlePercent) / 100.0));
    }

    public static boolean isForward(Trainset trainset) {
        return getDccThrottle(trainset).getIsForward();
    }

    private static DccThrottle getDccThrottle(Trainset trainset) {
        return ThrottleRegistry.getInstance().getThrottle(trainset.getAddress());
    }
}
