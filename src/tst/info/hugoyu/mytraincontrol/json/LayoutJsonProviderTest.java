package info.hugoyu.mytraincontrol.json;

import info.hugoyu.mytraincontrol.layout.GraphNode;
import info.hugoyu.mytraincontrol.layout.Layout;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

class LayoutJsonProviderTest {

    Layout layout;

    @Test
    void parseJSON() throws FileNotFoundException {
        layout = LayoutJsonProvider.parseJSON("layout.json");

        Map<String, GraphNode> nodes = layout.getNodes();
        assertEquals(10, nodes.size());

        GraphNode s1 = nodes.get("s1");
        assertNotNull(s1);
        Set<GraphNode> s1NextNodes = s1.getNextNodes();
        assertEquals(2, s1NextNodes.size());

        GraphNode regNode0 = nodes.get("0");
        assertNotNull(regNode0);
        Set<GraphNode> regNode0NextNodes = regNode0.getNextNodes();
        assertEquals(1, regNode0NextNodes.size());
    }
}