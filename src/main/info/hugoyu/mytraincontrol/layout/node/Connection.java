package info.hugoyu.mytraincontrol.layout.node;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class Connection {
    private long id0, id1;
    private int dist;
    private boolean isUplink;
    private boolean isBidirectional;
}
