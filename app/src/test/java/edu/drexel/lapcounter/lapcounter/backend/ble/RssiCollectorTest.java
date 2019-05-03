package edu.drexel.lapcounter.lapcounter.backend.ble;

import org.junit.Test;

import static org.junit.Assert.*;

public class RssiCollectorTest {

    @Test
    public void collect_does_not_add_element_when_disabled() {
        RssiCollector rc = new RssiCollector();
        rc.collect(54);
        RssiCollector.ListSizes ls = rc.getListSizes();
        assertEquals(0, ls.NumValues);
        assertEquals(0, ls.NumDeltas);
    }

    @Test
    public void collect_adds_element_when_enabled() {
        RssiCollector rc = new RssiCollector();
        rc.enable();
        rc.collect(54);
        RssiCollector.ListSizes ls = rc.getListSizes();
        assertEquals(1, ls.NumValues);
        assertEquals(1, ls.NumDeltas);
    }

    @Test
    public void collect_updates_min_and_max_of_one_element() {
        RssiCollector rc = new RssiCollector();
        rc.enable();

        int element = 54;
        rc.collect(element);

        assertEquals(element, rc.min());
        assertEquals(element, rc.max());
    }

    @Test
    public void collect_updates_min_and_max() {
        RssiCollector rc = new RssiCollector();
        rc.enable();

        int element = 54;
        rc.collect(element);

        assertEquals(element, rc.min());
        assertEquals(element, rc.max());

        int min = 32;
        rc.collect(min);
        assertEquals(min, rc.min());
        assertEquals(element, rc.max());

        int max = 76;
        rc.collect(max);
        assertEquals(min, rc.min());
        assertEquals(max, rc.max());
    }


    @Test
    public void isEnabled_false_when_disabled() {
        assertFalse(new RssiCollector().isEnabled());
    }

    @Test
    public void isEnabled_true_when_enabled() {
        RssiCollector rc = new RssiCollector();
        rc.enable();
        assertTrue(rc.isEnabled());
    }

    @Test
    public void isEnabled_false_when_enabled_then_disabled() {
        RssiCollector rc = new RssiCollector();
        rc.enable();
        assertTrue(rc.isEnabled());
        rc.disable();
        assertFalse(rc.isEnabled());
    }

    @Test
    public void clear_empties_buffers() {
        RssiCollector rc = new RssiCollector();
        rc.enable();
        rc.collect(54);
        rc.collect(73);
        rc.clear();
        RssiCollector.ListSizes ls = rc.getListSizes();
        assertEquals(0, ls.NumValues);
        assertEquals(0, ls.NumDeltas);
    }


    @Test
    public void clear_resets_min_and_max() {
        RssiCollector rc = new RssiCollector();
        rc.enable();

        int min = 54;
        int max = 73;

        rc.collect(min);
        rc.collect(max);

        assertEquals(min, rc.min());
        assertEquals(max, rc.max());

        rc.clear();

        assertEquals(Integer.MAX_VALUE, rc.min());
        assertEquals(Integer.MIN_VALUE, rc.max());
    }

    @Test
    public void clear_can_still_add_elements_afterwards() {
        RssiCollector rc = new RssiCollector();
        rc.enable();
        rc.collect(54);
        rc.collect(73);
        rc.clear();
        rc.collect(63);
        rc.collect(102);
        rc.collect(85);
        RssiCollector.ListSizes ls = rc.getListSizes();
        assertEquals(3, ls.NumValues);
        assertEquals(3, ls.NumDeltas);
    }

    @Test
    public void mean() {
    }

    @Test
    public void stdDev() {
    }

    @Test
    public void median() {
    }

    @Test
    public void min() {
    }

    @Test
    public void max() {
    }

//    @Test
//    public void toString() {
//    }
//
//    @Test
//    public void toString1() {
//    }

    @Test
    public void computeThreshold() {
    }
}