package info.hugoyu.mytraincontrol.util;

import info.hugoyu.mytraincontrol.LayoutTestBase;
import info.hugoyu.mytraincontrol.layout.Route;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RouteUtilTest extends LayoutTestBase {

    @Test
    public void findRouteToStationViaMergingTurnout() {
        Route route = RouteUtil.findRouteToStation(10103, "s2");

        List<Long> nodes = route.getNodes();
        assertEquals(4, nodes.size());
        assertEquals(Long.valueOf(10103), nodes.get(0));
        assertEquals(Long.valueOf(10003), nodes.get(1));
        assertEquals(Long.valueOf(10401), nodes.get(2));
        assertEquals(Long.valueOf(11101), nodes.get(3));

        assertEquals(2344, route.getCost());
    }

    @Test
    public void findRouteToStationViaDivergingTurnout() {
        Route route = RouteUtil.findRouteToStation(11101, "s1");

        List<Long> nodes = route.getNodes();
        assertEquals(3, nodes.size());
        assertEquals(Long.valueOf(11101), nodes.get(0));
        assertEquals(Long.valueOf(11103), nodes.get(1));
        assertEquals(Long.valueOf(10001), nodes.get(2));

        assertEquals(2520, route.getCost());
    }

    @Test
    public void findRouteToStationInvalidId() {
        assertThrows(RuntimeException.class, () -> RouteUtil.findRouteToStation(-1, "s2"));
        assertThrows(RuntimeException.class, () -> RouteUtil.findRouteToStation(10000, "invalid"));
    }

}