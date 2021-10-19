package info.hugoyu.mytraincontrol.layout.alias;

import info.hugoyu.mytraincontrol.LayoutTestBase;
import info.hugoyu.mytraincontrol.exception.InvalidIdException;
import info.hugoyu.mytraincontrol.util.LayoutUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class StationTest extends LayoutTestBase {

    @Test
    public void findAvailableTrack() {
        Station station = LayoutUtil.getStation("s1");

        assertEquals(10103, station.findRouteToAvailableTrack(10001, false).getDestinationNode());
        assertEquals(10101, station.findRouteToAvailableTrack(10001, true).getDestinationNode());

        assertEquals(10100, station.findRouteToAvailableTrack(10100, false).getDestinationNode());
        assertEquals(10100, station.findRouteToAvailableTrack(10100, true).getDestinationNode());

        assertThrows(InvalidIdException.class, () ->
                station.findRouteToAvailableTrack(10002, false));
    }
}