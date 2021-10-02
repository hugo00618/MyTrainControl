package info.hugoyu.mytraincontrol.json.layout;

import info.hugoyu.mytraincontrol.layout.node.impl.TurnoutNode;
import lombok.Getter;

@Getter
public class TurnoutJson {
    private long id0,id1,id2,id3;
    private int dist1, dist2;
    private TurnoutNode.Type type;
}
