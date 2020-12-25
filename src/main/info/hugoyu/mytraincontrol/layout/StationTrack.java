package info.hugoyu.mytraincontrol.layout;

import lombok.Getter;

import java.util.List;

@Getter
public class StationTrack {

    @Getter
    public static class ConnectingNode {
        private String id;
        private int cost;
    }

    private String id;
    private int length;
    private List<ConnectingNode> inboundNodes, outboundNodes;
}
