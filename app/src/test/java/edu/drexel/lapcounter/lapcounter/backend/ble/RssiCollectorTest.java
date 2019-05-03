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
    public void clear() {
    }

    @Test
    public void enable() {
    }

    @Test
    public void disable() {
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