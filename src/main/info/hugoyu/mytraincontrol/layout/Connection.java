package info.hugoyu.mytraincontrol.layout;

import lombok.Getter;

@Getter
public class Connection {

    private final Vector vector;
    private final int dist;
    private final boolean isUplink;
    private final boolean isBidirectional;

    public Connection(long id0, long id1, int dist, boolean isUplink, boolean isBidirectional) {
        this.vector = new Vector(id0, id1);
        this.dist = dist;
        this.isUplink = isUplink;
        this.isBidirectional = isBidirectional;
    }
}
