package info.hugoyu.mytraincontrol.layout.node.impl;

import info.hugoyu.mytraincontrol.LayoutTestBase;
import info.hugoyu.mytraincontrol.trainset.Trainset;
import info.hugoyu.mytraincontrol.util.LayoutUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

class StationTrackNodeTest extends LayoutTestBase {

    @Mock
    Trainset train1, train2;

    @BeforeEach
    public void setUp() {
        super.setUp();

        initMocks(this);

        when(train1.getAddress()).thenReturn(1);
        when(train2.getAddress()).thenReturn(2);
    }

    @Test
    public void testLayoutProvider() {
        StationTrackNode node = (StationTrackNode) LayoutUtil.getNode(10000);
        assertEquals(10000, node.getId());
        assertEquals("Track 1", node.getName());
        assertTrue(node.isPassingTrack());
        assertTrue(node.isPlatformTrack());
        assertEquals(992, node.getPlatformLength());
    }

}