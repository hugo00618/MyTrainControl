package info.hugoyu.mytraincontrol.util;

import info.hugoyu.mytraincontrol.LayoutTestBase;
import info.hugoyu.mytraincontrol.exception.InvalidIdException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LayoutUtilTest extends LayoutTestBase {

    @Test
    public void isReachable() {
        assertTrue(LayoutUtil.isReachable(10000, 10000));
        assertTrue(LayoutUtil.isReachable(10000, 10002));
        assertTrue(LayoutUtil.isReachable(10000, 10004));

        assertFalse(LayoutUtil.isReachable(10000, 10001));

        assertThrows(InvalidIdException.class, ()-> LayoutUtil.isReachable(-1, 10000));
        assertThrows(InvalidIdException.class, ()-> LayoutUtil.isReachable(10000, -1));
        assertThrows(InvalidIdException.class, ()-> LayoutUtil.isReachable(-1, -2));
    }

}