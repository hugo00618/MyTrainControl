package info.hugoyu.mytraincontrol.json.layout;

import lombok.Getter;

@Getter
public class SensorJson {
    private int address;
    private VectorJson node;
    private int offset;
}
