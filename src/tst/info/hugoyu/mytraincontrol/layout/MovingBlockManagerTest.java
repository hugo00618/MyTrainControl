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
        initMocks(this);

        TrainsetRegistry.getInstance().registerTrainset(3, trainset);
        when(trainset.getTotalLength()).thenReturn(960);
    }

    @Test
    public void getDistToMove() {
        // station track length: 1116
        Route route = RouteUtil.findRouteToStation(10100, "s2");
        MovingBlockManager sut = new MovingBlockManager(trainset);
        sut.prepareToMove(route);

        // outbound dist = (1116-960)/2 = 78
        // 78 + 1508 = 1586
        assertEquals(1586, sut.getDistToMove());
    }

}