package info.hugoyu.mytraincontrol.util;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MathUtilTest {

    private static final List<Double> DATA = List.of(1.0, 2.5, -2.1, 4.3, -3.8);
    @Test
    void removeOutliers() {
    }

    @Test
    void mean() {
        assertEquals(0.38, MathUtil.mean(DATA).doubleValue());
    }

    @Test
    void mean333() {
        assertEquals(3.3333, MathUtil.mean(List.of(10.0, 0.0, 0.0)).doubleValue()); // 3.333...
    }

    @Test
    void standardDeviation() {
        assertEquals(2.9620, MathUtil.standardDeviation(DATA).doubleValue(), Math.pow(1, -4));
    }
}