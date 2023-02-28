package info.hugoyu.mytraincontrol.util;


import com.google.common.collect.Range;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GeneralUtilTest {

    @Test
    public void testGetRandom() {
        List<Integer> list = List.of(1, 2, 3);

        // all elements in the list can possibly be returned by getRandom()
        // thr probability of calling getRandom() 23 times with a 3-element list and
        // not getting a particular element is less than 1/10,000
        assertTrue(
                list.stream().allMatch(ele -> {
                    for (int i = 0; i < 23; i++) {
                        if (GeneralUtil.getRandom(list).equals(ele)) {
                            return true;
                        }
                    }
                    return false;
                }));
    }

    @Test
    public void testIsOverlappingWithOverlappingRanges() {
        Range<Integer> r1 = Range.closed(1, 10),
                r2 = Range.closed(5, 12);
        assertTrue(GeneralUtil.isOverlapping(r1, r2));
    }

    @Test
    public void testIsOverlappingWithConnectedRanges() {
        Range<Integer> r1 = Range.closed(1, 10),
                r2 = Range.closed(10, 20);
        assertTrue(GeneralUtil.isOverlapping(r1, r2));
    }

    @Test
    public void testIsOverlappingWithAdjacentRanges() {
        Range<Integer> r1 = Range.closedOpen(1, 10),
                r2 = Range.closed(10, 20);
        assertFalse(GeneralUtil.isOverlapping(r1, r2));
    }

    @Test
    public void testIsOverlappingWithDisconnectedRanges() {
        Range<Integer> r1 = Range.closed(1, 10),
                r2 = Range.closed(30, 40);
        assertFalse(GeneralUtil.isOverlapping(r1, r2));
    }

}