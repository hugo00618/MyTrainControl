package info.hugoyu.mytraincontrol.layout;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StationTest extends LayoutTestBase{

    Station s1;

    StationTrack.ConnectingNode node1 = new StationTrack.ConnectingNode("3017", 0);
    StationTrack.ConnectingNode node2 = new StationTrack.ConnectingNode("6809", 0);

    @Override
    @BeforeEach
    public void setUp() throws IOException {
        super.setUp();
        s1 = layout.getStations().get("s1");
    }

    @Test
    void getInboundNodes() {
        Set<StationTrack.ConnectingNode> inboundNodes = s1.getInboundNodes();

        assertEquals(2, inboundNodes.size());
        assertTrue(inboundNodes.contains(node1));
        assertTrue(inboundNodes.contains(node2));
    }

    @Test
    void getTracks() {
        StationTrack track = s1.getTrack(node1);
        assertEquals("t1", track.getId());
    }
}