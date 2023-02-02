package info.hugoyu.mytraincontrol.registry;

import info.hugoyu.mytraincontrol.LayoutTestBase;
import info.hugoyu.mytraincontrol.layout.alias.Station;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LayoutRegistryTest extends LayoutTestBase {

    @Test
    public void getLayout() {
        LayoutRegistry layoutRegistry = LayoutRegistry.getInstance();
        Map<String, Station> stations = layoutRegistry.getStations();

        assertEquals(2, stations.size());
    }
}