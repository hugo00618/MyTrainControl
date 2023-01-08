package info.hugoyu.mytraincontrol.json.layout;

import lombok.Getter;

@Getter
public class SensorJson {
    private int address;
    private long nodeId;
    private boolean isUplink;
    private int offset;
}
