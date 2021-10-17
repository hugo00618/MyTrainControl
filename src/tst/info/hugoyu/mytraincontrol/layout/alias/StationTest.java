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

        assertEquals(10000, station.findAvailableTrack(10000, false).getId());
        assertEquals(10000, station.findAvailableTrack(10000, true).getId());

        assertEquals(10001, station.findAvailableTrack(10001, false).getId());
        assertEquals(10001, station.findAvailableTrack(10001, true).getId());

        assertThrows(InvalidIdException.class, () ->
                station.findAvailableTrack(10002, false));
    }
}