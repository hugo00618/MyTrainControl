package info.hugoyu.mytraincontrol.layout;

import info.hugoyu.mytraincontrol.layout.node.AbstractGraphNode;
import info.hugoyu.mytraincontrol.layout.node.impl.RegularNode;
import info.hugoyu.mytraincontrol.layout.node.impl.StationNode;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LayoutTest extends LayoutTestBase{

    @Test
    void postDeserialization() {
        Map<String, AbstractGraphNode> nodes = layout.getNodes();
        assertEquals(8, nodes.size());

        AbstractGraphNode regularNode = nodes.get("0");
        assertTrue(regularNode instanceof RegularNode);
        Set<String> regularNodeNextNodes = regularNode.getNextNodes();
        assertEquals(1, regularNodeNextNodes.size());

        AbstractGraphNode stationNode = nodes.get("1508");
        assertTrue(stationNode instanceof StationNode);
        Set<String> stationNodeNextNodes = stationNode.getNextNodes();
        assertEquals(1, stationNodeNextNodes.size());
    }

}