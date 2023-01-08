package info.hugoyu.mytraincontrol.json.layout;

import lombok.Getter;

import java.util.Map;

@Getter
public class RegularTrackJson {
    private long id0, id1;
    private int length;
    private boolean isBidirectional;

    // map of (sensorAddress, location)
    private Map<Integer, Integer> sensors;
}
