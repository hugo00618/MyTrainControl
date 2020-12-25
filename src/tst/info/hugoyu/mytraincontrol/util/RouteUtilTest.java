package info.hugoyu.mytraincontrol.util;

import info.hugoyu.mytraincontrol.json.LayoutJsonProvider;
import info.hugoyu.mytraincontrol.layout.GraphNode;
import info.hugoyu.mytraincontrol.layout.Layout;
import info.hugoyu.mytraincontrol.layout.Route;
import org.junit.Before;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.util.Map;

import static org.junit.Assert.assertEquals;

class RouteUtilTest {

    Layout layout;

    @Before
    public void setUp() throws FileNotFoundException {
        layout = LayoutJsonProvider.parseJSON("layout.json");
    }

    @Test
    public void findRoute() {
        Map<String, GraphNode> nodes = layout.getNodes();

        GraphNode s1 = nodes.get("s1");
        GraphNode s2 = nodes.get("s2");
        GraphNode regNode1 = nodes.get("0");
        GraphNode regNode2 = nodes.get("1509");
        GraphNode regNode3 = nodes.get("6809");
        GraphNode regNode4 = nodes.get("5404");

        Route r1 = RouteUtil.findRoute(s1, s2);
        assertEquals(4, r1.getRouteNodes().size());
        assertEquals(1508, r1.getCost());
    }
}