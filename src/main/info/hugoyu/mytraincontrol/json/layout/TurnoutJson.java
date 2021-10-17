package info.hugoyu.mytraincontrol.json.layout;

import info.hugoyu.mytraincontrol.layout.node.impl.TurnoutNode;
import lombok.Getter;

@Getter
public class TurnoutJson {
    /**
     * Turnout id
     */
    private long id0;

    /**
     * Destination node ids
     */
    private long id1, id2;

    /**
     * Dummy outbound id, if the turnout is a merging turnout
     */
    private Long id3;

    private int dist1, dist2;
    private TurnoutNode.Type type;
}
