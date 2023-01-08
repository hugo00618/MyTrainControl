package info.hugoyu.mytraincontrol.json.layout;

import lombok.Getter;

import java.util.List;

@Getter
public class CrossoverJson {

    @Getter
    public static class CrossConnectionJson {
        private long id0, id1;
        private int dist;
        private boolean isBidirectional;
    }

    private long uplinkId0, uplinkId1;
    private long downlinkId0, downlinkId1;

    private int length;

    private int address;

    private List<CrossConnectionJson> uplinkCrosses, downlinkCrosses;
}
