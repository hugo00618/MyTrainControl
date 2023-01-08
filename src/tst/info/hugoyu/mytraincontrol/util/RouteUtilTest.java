package info.hugoyu.mytraincontrol.util;

import info.hugoyu.mytraincontrol.LayoutTestBase;
import info.hugoyu.mytraincontrol.layout.Route;
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
    public void testFindRouteToStationViaTurnouts() {
        Route route = RouteUtil.findRouteToStation(trainset, 121, "s2");

        assertEquals(List.of(121L, 123L, 135L, 147L, 149L, 151L, 153L, 155L), route.getNodes());
        assertFalse(route.isUplink());
        assertEquals(3297, route.getCost());
    }

    @Test
    public void testFindRouteToStationViaDoubleCrossover() {
        Route route = RouteUtil.findRouteToStation(trainset, 154, "s2");

        assertTrue(List.of(154L, 151L, 153L, 155L).equals(route.getNodes()));
        assertFalse(route.isUplink());
        assertEquals(1923, route.getCost());
    }

    @Test
    public void testFindReachableStations() {
        when(trainset.getLastAllocatedNodeId()).thenReturn(154L);

        List<Route> routes = RouteUtil.findReachableStations(trainset);

        assertEquals(1, routes.size());
    }

    @Test
    public void testFindRouteToAvailableStationTrackWithLongTrainset() {
        when(trainset.getTotalLength()).thenReturn(1300);

        Route route = RouteUtil.findRouteToAvailableStationTrack(trainset, 152,
                false, true);

        assertEquals(List.of(152L, 154L), route.getNodes());
        assertTrue(route.isUplink());
        assertEquals(310, route.getCost());
    }

    @Test
    public void testFindRouteToAvailableStationTrackWithShortTrainset() {
        when(trainset.getTotalLength()).thenReturn(1);

        Route route = RouteUtil.findRouteToAvailableStationTrack(trainset, 152,
                false, true);

        assertEquals(List.of(152L, 149L, 147L, 135L, 133L, 131L), route.getNodes());
        assertTrue(route.isUplink());
        assertEquals(1686, route.getCost());
    }

}