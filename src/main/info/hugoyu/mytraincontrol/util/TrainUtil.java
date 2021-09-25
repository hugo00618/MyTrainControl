package info.hugoyu.mytraincontrol.util;

import info.hugoyu.mytraincontrol.layout.Route;
import info.hugoyu.mytraincontrol.layout.node.track.AbstractTrackNode;
import info.hugoyu.mytraincontrol.layout.node.track.impl.StationTrackNode;
import info.hugoyu.mytraincontrol.registry.ThrottleRegistry;
import info.hugoyu.mytraincontrol.registry.TrainsetRegistry;
import info.hugoyu.mytraincontrol.trainset.Trainset;

import java.util.List;

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

    public static boolean allocateStationTrackImmediate(int address, long trackNodeId) {
        Trainset trainset = TrainsetRegistry.getInstance().getTrainset(address);

        AbstractTrackNode node = LayoutUtil.getNode(trackNodeId);
        if (!(node instanceof StationTrackNode)) {
            throw new RuntimeException("Node id does not map to a station track node: " + trackNodeId);
        }

        StationTrackNode stationTrackNode = (StationTrackNode) node;
        return stationTrackNode.reserve(trainset);
    }

    public static void moveTo(int address, String stationId) {
        Trainset trainset = TrainsetRegistry.getInstance().getTrainset(address);

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
