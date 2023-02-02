package info.hugoyu.mytraincontrol.layout.node;

import info.hugoyu.mytraincontrol.layout.Connection;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class ConnectionTest {

    private Connection c1, c2;

    @Test
    public void testEqualsTrue() {
        initEqualC1C2();

        assertEquals(c1, c2);
    }

    @Test
    public void testEqualsFalse() {
        initNotEqualC1C2();

        assertNotEquals(c1, c2);
    }

    @Test
    public void testHashCodeEqual() {
        initEqualC1C2();

        assertEquals(c1.hashCode(), c2.hashCode());
    }

    @Test
    public void testHashCodeNotEqual() {
        initNotEqualC1C2();

        assertNotEquals(c1.hashCode(), c2.hashCode());
    }

    private void initEqualC1C2() {
        c1 = new Connection(0, 1, 100, true, false);
        c2 = new Connection(0, 1, 200, false, true);
    }

    private void initNotEqualC1C2() {
        c1 = new Connection(0, 1, 100, true, false);
        c2 = new Connection(0, 2, 100, true, false);
    }



}