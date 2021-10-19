package info.hugoyu.mytraincontrol.layout.node.impl;

import info.hugoyu.mytraincontrol.LayoutTestBase;
import info.hugoyu.mytraincontrol.util.LayoutUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StationTrackNodeTest extends LayoutTestBase {

    @Test
    public void testLayoutProvider() {
        StationTrackNode node = (StationTrackNode) LayoutUtil.getNode(10103);
        assertEquals(10103, node.getId());
        assertEquals("Track 1", node.getName());
        assertFalse(node.isPassingTrack());
        assertTrue(node.isPlatformTrack());
        assertEquals(752, node.getPlatformLength());
    }

}