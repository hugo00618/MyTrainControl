package info.hugoyu.mytraincontrol.json.layout;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CrossoverJson {
    private int length, crossLength;

    private int address;

    private VectorJson uplinkStraight, downlinkStraight, uplinkCross, downlinkCross;
}
