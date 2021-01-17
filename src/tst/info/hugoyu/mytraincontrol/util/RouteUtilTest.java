package info.hugoyu.mytraincontrol.util;

import info.hugoyu.mytraincontrol.layout.LayoutTestBase;
import info.hugoyu.mytraincontrol.layout.Route;
import info.hugoyu.mytraincontrol.layout.node.AbstractGraphNode;
import info.hugoyu.mytraincontrol.trainset.Trainset;
import info.hugoyu.mytraincontrol.trainset.TrainsetProfile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

class RouteUtilTest extends LayoutTestBase {

    Map<String, AbstractGraphNode> nodes;

    @Mock
    Trainset trainset;

    @Mock
    TrainsetProfile trainsetProfile;

    @Override
    @BeforeEach
    public void setUp() throws IOException {
        super.setUp();
        nodes = layout.getNodes();

        initMocks(this);

        when(trainset.getProfile()).thenReturn(trainsetProfile);
        when(trainsetProfile.getTotalLength()).thenReturn(0);
    }

    /**
     * Find route from node 0 to s2
     * Expected route: 0, 1508
     */
    @Test
    public void findRouteNode0toS2() {
        Route route = RouteUtil.findRouteToStation(trainset, "0", "s2");
        assertEquals(2, route.getRouteNodes().size());
        assertEquals(1508, route.getCost());
    }

    /**
     * Find route from node 0 to s1
     * Expected route: 0, 1508, 1509, 3017
     */
    @Test
    public void findRoute() {
        Route route = RouteUtil.findRouteToStation(trainset, "0", "s1");
        assertEquals(4, route.getRouteNodes().size());
        assertEquals(4008, route.getCost());
    }
}