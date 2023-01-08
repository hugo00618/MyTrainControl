package info.hugoyu.mytraincontrol.layout;

import info.hugoyu.mytraincontrol.LayoutTestBase;
import info.hugoyu.mytraincontrol.layout.movingblock.MovingBlockManager;
import info.hugoyu.mytraincontrol.registry.TrainsetRegistry;
import info.hugoyu.mytraincontrol.trainset.Trainset;
import info.hugoyu.mytraincontrol.util.RouteUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

class MovingBlockManagerTest extends LayoutTestBase {

    @Mock
    Trainset trainset;

    @BeforeEach
    public void setUp() {
        super.setUp();

        initMocks(this);

        TrainsetRegistry.getInstance().registerTrainset(3, trainset);
        when(trainset.getTotalLength()).thenReturn(960);
    }

    @Test
    public void getDistToMove() {
        // station track length: 1304
        // route length: 1923
        // route is downlink, station track is uplink
        Route route = RouteUtil.findRouteToStation(trainset, 154, "s2");
        MovingBlockManager sut = new MovingBlockManager(trainset);
        sut.prepareToMove(route);

        // outbound dist = (1304-960)/2 + 960 = 1132
        // distToMove is 1923 + 1132 = 3055
        assertEquals(3055, sut.getDistToMove());
    }

}