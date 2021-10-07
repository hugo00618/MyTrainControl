package info.hugoyu.mytraincontrol.util;

import info.hugoyu.mytraincontrol.LayoutTestBase;
import info.hugoyu.mytraincontrol.layout.Route;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RouteUtilTest extends LayoutTestBase {

    @Test
    public void findRouteToStation() {
        Route route = RouteUtil.findRouteToStation(10000, "s2");

        List<Long> nodes = route.getNodes();
        assertEquals(3, nodes.size());
        assertEquals(Long.valueOf(10000), nodes.get(0));
        assertEquals(Long.valueOf(10002), nodes.get(1));
        assertEquals(Long.valueOf(10004), nodes.get(2));

        assertEquals(1508, route.getMoveDist());
    }

    @Test
    public void findRouteToStationInvalidId() {
        assertThrows(RuntimeException.class, () -> RouteUtil.findRouteToStation(-1, "s2"));
        assertThrows(RuntimeException.class, () -> RouteUtil.findRouteToStation(10000, "invalid"));
    }

    @Test
    public void findInboundRoute() {
        Route route = RouteUtil.findInboundRoute(10000, LayoutUtil.getStationTrackNode(10000));

        assertEquals(2, route.getNodes().size());
        assertEquals(10000, route.getNodes().get(0));
        assertEquals(10002, route.getNodes().get(1));
    }

}