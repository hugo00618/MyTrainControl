package info.hugoyu.mytraincontrol.util;

import info.hugoyu.mytraincontrol.commandstation.task.impl.SetThrottleTask;
import info.hugoyu.mytraincontrol.exception.RouteException;
import info.hugoyu.mytraincontrol.layout.Route;
import info.hugoyu.mytraincontrol.registry.ThrottleRegistry;
import info.hugoyu.mytraincontrol.registry.TrainsetRegistry;
import info.hugoyu.mytraincontrol.trainset.Trainset;
import info.hugoyu.mytraincontrol.trainset.TrainsetProfile;
import jmri.DccThrottle;

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
        String filePath = TRAINSET_PROFILE_DIR + "/" + fileName;
        TrainsetProfile trainsetProfile = FileUtil.readJson(filePath, TrainsetProfile.class);
        trainsetProfile.postDeserialization();
        return trainsetProfile;
    }

    public static Map<Integer, Trainset> getTrainsets() {
        return TrainsetRegistry.getInstance().getTrainsets();
    }

    public static void setLight(Trainset trainset, LightState lightState) {
        trainset.setIsLightOn(lightState);
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
        Route route = RouteUtil.findRouteToStation(trainset, trainset.getAllocatedStationTrack().get(), stationId);
        if (route == null) {
            throw new RouteException(String.format("No route to station: %s", stationId));
        }

        moveTo(trainset, route);
    }

    public static void moveTo(Trainset trainset, Route route) {
        if (route.getNodes().size() == 1) {
            throw new RouteException(String.format("Train is already at node: %d", route.getNodes().get(0)));
        }

        trainset.move(route);
    }

    public static void setThrottle(Trainset trainset, int throttlePercent) {
        CommandStationUtil.addTask(new SetThrottleTask(getDccThrottle(trainset), throttlePercent));
    }

    private static DccThrottle getDccThrottle(Trainset trainset) {
        return ThrottleRegistry.getInstance().getThrottle(trainset.getAddress());
    }
}
