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
        assertTrue(LayoutUtil.isReachable(10101, 10101));
        assertTrue(LayoutUtil.isReachable(10101, 11101));
        assertTrue(LayoutUtil.isReachable(10101, 11103));

        assertFalse(LayoutUtil.isReachable(10101, 10100));

        assertThrows(InvalidIdException.class, ()-> LayoutUtil.isReachable(-1, 10101));
        assertThrows(InvalidIdException.class, ()-> LayoutUtil.isReachable(10101, -1));
        assertThrows(InvalidIdException.class, ()-> LayoutUtil.isReachable(-1, -2));
    }

}