package info.hugoyu.mytraincontrol.util;

import info.hugoyu.mytraincontrol.layout.Route;
import info.hugoyu.mytraincontrol.layout.node.impl.StationNode;
import info.hugoyu.mytraincontrol.registry.ThrottleRegistry;
import info.hugoyu.mytraincontrol.registry.TrainsetRegistry;
import info.hugoyu.mytraincontrol.trainset.Trainset;

public class TrainUtil {

    public static void moveDist(int address, int dist) {
        Trainset trainset = TrainsetRegistry.getInstance().getTrainset(address);
        trainset.move(dist);
    }

    public static void registerTrainset(int address, String name, String profileFilename) throws Exception {
        TrainsetRegistry.getInstance().registerTrainset(address, name, profileFilename);
        ThrottleRegistry.getInstance().registerThrottle(address);
    }

    public static void setLight(int address, boolean on) {
        Trainset trainset = TrainsetRegistry.getInstance().getTrainset(address);
        trainset.setIsLightOn(on);
    }

    public static boolean allocateStationTrackImmediate(int address, String trackId) {
        Trainset trainset = TrainsetRegistry.getInstance().getTrainset(address);
        String trackNodeId = LayoutUtil.getLayoutStationTracks().get(trackId).getInboundNode().getId();
        StationNode trackNode = (StationNode) LayoutUtil.getNode(trackNodeId);
        boolean isAllocated = trackNode.reserve(trainset);
        if (isAllocated) {
            trainset.getAllocatedNodes().add(trackNodeId);
        }
        return isAllocated;
    }

    public static void moveTo(int address, String stationId) throws Exception {
        Trainset trainset = TrainsetRegistry.getInstance().getTrainset(address);
        String fromNodeId = trainset.getAllocatedNodes().stream()
                .filter(nodeId -> LayoutUtil.getNode(nodeId) instanceof StationNode)
                .findFirst()
                .orElse(null);
        if (fromNodeId == null) {
            throw new Exception("Trainset is not in a station");
        }

        Route route = RouteUtil.findRouteToStation(trainset, fromNodeId, stationId);
        if (route == null) {
            throw new Exception("No route to station");
        }

        trainset.move(route);
    }
}
