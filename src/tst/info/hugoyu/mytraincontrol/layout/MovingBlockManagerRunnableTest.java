package info.hugoyu.mytraincontrol.layout;

import info.hugoyu.mytraincontrol.LayoutTestBase;
import info.hugoyu.mytraincontrol.registry.TrainsetRegistry;
import info.hugoyu.mytraincontrol.trainset.Trainset;
import info.hugoyu.mytraincontrol.util.RouteUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

class MovingBlockManagerRunnableTest extends LayoutTestBase {

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
        Route route = RouteUtil.findRouteToStation(10000, "s2");
        MovingBlockManagerRunnable sut = new MovingBlockManagerRunnable(trainset);
        sut.prepareToMove(route);

        assertEquals(1524, sut.getDistToMove());
    }

}