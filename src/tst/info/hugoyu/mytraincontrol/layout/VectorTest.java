package info.hugoyu.mytraincontrol.layout;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class VectorTest {

    @Test
    public void testEqualsTrue() {
        Vector v1 = new Vector(1, 2);
        Vector v2 = new Vector(1, 2);

        assertEquals(v1, v2);
    }

    @Test
    public void testEqualsFalse() {
        Vector v1 = new Vector(1, 2);
        Vector v2 = new Vector(3, 4);

        assertNotEquals(v1, v2);
    }

    @Test
    public void testHashCodeEqualsTrue() {
        Vector v1 = new Vector(1, 2);
        Vector v2 = new Vector(1, 2);

        assertEquals(v1.hashCode(), v2.hashCode());
    }

    @Test
    public void testHashCodeEqualsFalse() {
        Vector v1 = new Vector(1, 2);
        Vector v2 = new Vector(3, 4);

        assertNotEquals(v1.hashCode(), v2.hashCode());
    }

}