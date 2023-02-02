package info.hugoyu.mytraincontrol.util;

import info.hugoyu.mytraincontrol.LayoutTestBase;
import info.hugoyu.mytraincontrol.layout.Route;
import info.hugoyu.mytraincontrol.layout.Vector;
import info.hugoyu.mytraincontrol.trainset.Trainset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

class RouteUtilTest extends LayoutTestBase {

    @Mock
    Trainset trainset;

    @BeforeEach
    public void setUp() {
        super.setUp();

        initMocks(this);
    }

    @Test
    public void testFindRouteToStationDownlinkToDownLinkViaTurnouts() {
        Route route = RouteUtil.findRouteToStation(trainset, LayoutUtil.getStationTrackNode(new Vector(121, 123)), "s2");

        assertEquals(List.of(123L, 135L, 147L, 149L, 151L, 153L, 155L), route.getNodes());
        assertFalse(route.isUplink());
        assertEquals(2549, route.getCost());
    }

    @Test
    public void testFindRouteToStationUplinkToDownLinkViaDoubleCrossover() {
        Route route = RouteUtil.findRouteToStation(trainset, LayoutUtil.getStationTrackNode(new Vector(154, 156)), "s2");

        assertEquals(List.of(154L, 151L, 153L, 155L), route.getNodes());
        assertFalse(route.isUplink());
        assertEquals(1923, route.getCost());
    }

    @Test
    public void testFindRouteToStationUplinkToUplink() {
        Route route = RouteUtil.findRouteToStation(trainset, LayoutUtil.getStationTrackNode(new Vector(146, 148)), "s1");

        assertEquals(List.of(148L, 150L, 152L), route.getNodes());
        assertTrue(route.isUplink());
        assertEquals(1507, route.getCost());
    }

    @Test
    public void testFindReachableStations() {
        List<Route> routes = RouteUtil.findReachableStations(trainset, LayoutUtil.getStationTrackNode(new Vector(154, 156)));

        assertEquals(1, routes.size());
    }

    @Test
    public void testFindReachableStationsWithLongTrainset() {
        when(trainset.getTotalLength()).thenReturn(1300);
        List<Route> routes = RouteUtil.findReachableStations(trainset, LayoutUtil.getStationTrackNode(new Vector(154, 156)));

        assertEquals(0, routes.size());
    }

    @Test
    public void testFindRouteToAvailableStationTrackWithLongTrainset() {
        when(trainset.getTotalLength()).thenReturn(1300);

        Route route = RouteUtil.findRouteToAvailableStationTrack(
                trainset,
                152,
                true,
                false,
                true);

        assertEquals(List.of(152L, 154L, 156L), route.getNodes());
        assertTrue(route.isUplink());
        assertEquals(1614, route.getCost());
    }

    @Test
    public void testFindRouteToAvailableStationTrackWithShortTrainset() {
        when(trainset.getTotalLength()).thenReturn(1);

        Route route = RouteUtil.findRouteToAvailableStationTrack(
                trainset, 152,
                true,
                false,
                true);

        assertEquals(List.of(152L, 149L, 147L, 135L, 133L, 131L), route.getNodes());
        assertTrue(route.isUplink());
        assertEquals(1686, route.getCost());
    }

}