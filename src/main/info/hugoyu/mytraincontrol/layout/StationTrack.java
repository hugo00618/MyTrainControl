package info.hugoyu.mytraincontrol.layout;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
public class StationTrack {

    @EqualsAndHashCode
    @AllArgsConstructor
    @Getter
    public static class ConnectingNode {
        private String id;
        private int cost;
    }

    private String name;
    private String id;
    private int length;
    private boolean isPassingTrack;
    private boolean isPlatformTrack;
    private ConnectingNode inboundNode, outboundNode;
}
