package info.hugoyu.mytraincontrol.registry;

import info.hugoyu.mytraincontrol.LayoutTestBase;
import info.hugoyu.mytraincontrol.layout.alias.Station;
import info.hugoyu.mytraincontrol.layout.node.AbstractTrackNode;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LayoutRegistryTest extends LayoutTestBase {

    @Test
    public void getLayout() {
        LayoutRegistry layoutRegistry = LayoutRegistry.getInstance();
        Map<Long, AbstractTrackNode> nodes = layoutRegistry.getNodes();
        Map<String, Station> stations = layoutRegistry.getStations();

        assertEquals(11, nodes.size());
        assertEquals(2, stations.size());
    }
}