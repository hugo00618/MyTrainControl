package info.hugoyu.mytraincontrol.trainset;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TrainsetProfileTest {

    TrainsetProfile sut;

    @BeforeEach
    public void setUp() {
        sut = new TrainsetProfile();

        sut.setThrottleSpeedMap(ImmutableMap.of(
                1,5.0,
                2, 10.0,
                4, 19.0
        ));
        sut.setSpeedThrottleMap(new TreeMap<Double, Integer>(){{
            put(5.0, 1);
            put(10.0, 2);
            put(19.0, 4);
        }});
    }

    @Test
    public void testGetTotalLength() {
        sut.setControlCarLengths(new int[]{100, 120});
        sut.setPassengerCarLengths(new int[]{80, 90});
        sut.setNumControlCars(new int[]{2, 2});
        sut.setNumPassengerCars(new int[]{7, 10});

        final int EXPECTED_TOTAL_LENGTH = 100 * 2 + 120 * 2 + 80 * 7 + 90 * 10;

        assertEquals(EXPECTED_TOTAL_LENGTH, sut.getTotalLength());
    }

    @Test
    public void testGetThrottleLowerThanMinSpeed() {
        assertEquals(0.01f, sut.getThrottle(2));
    }

    @Test
    public void testGetThrottleExactMatch() {
        assertEquals(0.02f, sut.getThrottle(10));
    }

    @Test
    public void testGetThrottleIntermediate() {
        assertEquals(0.03f, sut.getThrottle(16));
    }

    @Test
    public void testGetThrottleHigherThanMaxSpeed() {
        assertEquals(0.04f, sut.getThrottle(21));
    }

}