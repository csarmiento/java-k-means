package co.com.runtime.kmeans.test;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;
import co.com.runtime.kmeans.datatypes.Baloto;

public class BalotoTest extends TestCase {
    public void testDistance() {
        Baloto b1 = new Baloto("2 6 7 17 31 38");
        Baloto b2 = new Baloto("7 9 11 19 24 42");

        assertEquals(Math.sqrt(119), b1.distance(b2));

        assertEquals(b1.distance(b2), b2.distance(b1));

        System.out.println(b1.distance(b2));
    }

    public void testMean() {
        Baloto b1 = new Baloto("2 6 7 17 31 38");
        Baloto b2 = new Baloto("7 9 11 19 24 42");
        Baloto b3 = new Baloto("6 8 21 28 34 41");

        Map<Baloto, Boolean> elements = new HashMap<Baloto, Boolean>();
        elements.put(b1, true);
        elements.put(b2, true);

        assertEquals(new Baloto("4.5 7.5 9 18 27.5 40"), b3.mean(elements));
        System.out.println(b3.mean(elements));
        elements.put(b3, true);

        assertEquals(
                new Baloto(
                        "5.0 7.666666666666667 13.0 21.333333333333332 29.666666666666668 40.333333333333336"),
                b2.mean(elements));
        System.out.println(b3.mean(elements));
    }

    public void testEquals() {
        Baloto b1 = new Baloto("2 6 7 17 31 38");
        Baloto b2 = new Baloto("7 9 11 19 24 42");
        Baloto b3 = new Baloto("17 31 38 2 6 7");

        assertFalse(b1.equals(b2));

        assertTrue(b1.equals(b3));
    }
}
