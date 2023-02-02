package info.hugoyu.mytraincontrol.json.layout;

import info.hugoyu.mytraincontrol.layout.node.impl.TurnoutNode;
import lombok.Getter;

import java.util.Map;

@Getter
public class TurnoutJson {
    /**
     * Turnout id
     */
    private long id;

    /**
     * Destination node ids
     */
    private long idClosed, idThrown;

    private int distClosed, distThrown;

    private TurnoutNode.Type type;

    private int address;

    // map of (sensorAddress, location)
    private Map<Integer, Integer> sensors;
}
