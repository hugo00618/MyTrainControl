package info.hugoyu.mytraincontrol.util;

import info.hugoyu.mytraincontrol.exception.NodeAllocationException;
import info.hugoyu.mytraincontrol.json.LayoutJsonProvider;
import info.hugoyu.mytraincontrol.layout.Layout;
import info.hugoyu.mytraincontrol.layout.Station;
import info.hugoyu.mytraincontrol.layout.StationTrack;
import info.hugoyu.mytraincontrol.layout.node.AbstractGraphNode;
import info.hugoyu.mytraincontrol.layout.node.BlockSectionResult;
import info.hugoyu.mytraincontrol.trainset.Trainset;

import java.io.IOException;
import java.util.Map;

public class LayoutUtil {

    private static Layout layout;

    static {
        try {
            layout = LayoutJsonProvider.parseJSON("layout.json");
        } catch (IOException e) {

        }
    }

    public static Map<String, AbstractGraphNode> getNodes() {
        return layout.getNodes();
    }

    public static AbstractGraphNode getNode(String nodeId) {
        return layout.getNodes().get(nodeId);
    }

    public static BlockSectionResult allocNode(String nodeId, Trainset trainset, int dist, String nextNodeId)
            throws NodeAllocationException {
        return getNode(nodeId).alloc(trainset, dist, nextNodeId);
    }

    public static BlockSectionResult freeNode(String nodeId, Trainset trainset, int dist)
            throws NodeAllocationException {
        return getNode(nodeId).free(trainset, dist);
    }

    public static Map<String, Station> getStations() {
        return layout.getStations();
    }

    public static Map<String, StationTrack> getLayoutStationTracks() {
        return layout.getStationTracks();
    }

}
