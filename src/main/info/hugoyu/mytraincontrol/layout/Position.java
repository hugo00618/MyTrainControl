package info.hugoyu.mytraincontrol.layout;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * a vector pointing from "referenceNode" with direction "isUplink" and length "offset"
 */
@AllArgsConstructor
@Getter
public class Position {
    private Vector referenceNodeVector;
    private boolean isUplink;
    private int offset;
}
