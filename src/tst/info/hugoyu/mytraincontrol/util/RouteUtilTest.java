package info.hugoyu.mytraincontrol.util;

import info.hugoyu.mytraincontrol.LayoutTestBase;
import info.hugoyu.mytraincontrol.layout.Route;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RouteUtilTest extends LayoutTestBase {

    @Test
    public void findRouteToNode() {
        Route route = RouteUtil.findRouteToNode(10000, 10006);

        assertEquals(3492, route.getCost());

        List<Long> nodes = route.getNodes();
        assertEquals(4, nodes.size());
        assertEquals(10000, nodes.get(0));
        assertEquals(10002, nodes.get(1));
        assertEquals(10004, nodes.get(2));
        assertEquals(10006, nodes.get(3));
    }

    @Test
    public void findRouteToNodeUnreachable() {
        Route route = RouteUtil.findRouteToNode(10000, 10001);
        assertNull(route);
    }

    @Test
    public void findRouteToNodeInvalid() {
        Route route = RouteUtil.findRouteToNode(10000, -1);
        assertNull(route);
    }

    @Test
    public void findRouteToStation() {
        Route route = RouteUtil.findRouteToStation(10000, "s2");

        assertEquals(2500, route.getCost());

        List<Long> nodes = route.getNodes();
        assertEquals(3, nodes.size());
        assertEquals(10000, nodes.get(0));
        assertEquals(10002, nodes.get(1));
        assertEquals(10004, nodes.get(2));
    }

    @Test
    public void findRouteToStationInvalid() {
        assertThrows(RuntimeException.class, () -> RouteUtil.findRouteToStation(10000, "invalid"));
    }

}