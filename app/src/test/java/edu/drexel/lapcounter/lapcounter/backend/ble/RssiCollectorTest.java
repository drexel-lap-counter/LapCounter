package edu.drexel.lapcounter.lapcounter.backend.ble;

import android.util.CustomAssertions;

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
    public void mean_of_zero_elements_is_zero() {
        CustomAssertions.assertEquals(0, new RssiCollector().mean());
    }

    @Test
    public void mean_of_one_element_is_that_element() {
        RssiCollector rc = new RssiCollector();
        rc.enable();
        int element = 17;
        rc.collect(element);
        CustomAssertions.assertEquals(element, rc.mean());
    }

    @Test
    public void mean_of_n_elements() {
        RssiCollector rc = new RssiCollector();
        rc.enable();

        int n = 100;
        for (int i = 1; i <= n; ++i) {
            rc.collect(i);
        }

        double expected_mean = (n + 1) / 2.0;
        CustomAssertions.assertEquals(expected_mean, rc.mean());
    }


    @Test
    public void stdDev_of_zero_elements_is_zero() {
        int unused_mean = -1;
        CustomAssertions.assertEquals(0, new RssiCollector().stdDev(unused_mean));
    }

    @Test
    public void stdDev_of_one_element_is_zero() {
        RssiCollector rc = new RssiCollector();
        rc.enable();
        rc.collect(10);
        int unused_mean = -1;

        CustomAssertions.assertEquals(0, rc.stdDev(unused_mean));
    }

    @Test
    public void stdDev_of_n_elements() {
        RssiCollector rc = new RssiCollector();
        rc.enable();

        int n = 100;
        for (int i = 1; i <= n; ++i) {
            rc.collect(i);
        }

        final double mean = rc.mean();

        double expected_deviations_sum = (n + 1);
        expected_deviations_sum *= (2 * n + 1) / 6.0 - mean;
        expected_deviations_sum += mean * mean;
        expected_deviations_sum *= n;

        double expected_std_dev = Math.sqrt(expected_deviations_sum / (n - 1));

        CustomAssertions.assertEquals(expected_std_dev, rc.stdDev(mean));
    }

    @Test
    public void median_of_zero_elements_is_zero() {
        CustomAssertions.assertEquals(0, new RssiCollector().median());
    }

    @Test
    public void median_of_one_element_is_that_element() {
        RssiCollector rc = new RssiCollector();
        rc.enable();
        int element = 17;
        rc.collect(element);
        CustomAssertions.assertEquals(element, rc.median());
    }

    @Test
    public void median_of_n_elements_odd() {
        RssiCollector rc = new RssiCollector();
        rc.enable();

        int n = 9;
        for (int i = 1; i <= n; ++i) {
            rc.collect(i);
        }

        double expected_median = (n + 1) / 2.0;
        CustomAssertions.assertEquals(expected_median, rc.median());
    }

    @Test
    public void median_of_n_elements_even() {
        RssiCollector rc = new RssiCollector();
        rc.enable();

        int n = 10;
        for (int i = 1; i <= n; ++i) {
            rc.collect(i);
        }

        double expected_median = (n + 1) / 2.0;
        CustomAssertions.assertEquals(expected_median, rc.median());
    }

//    @Test
//    public void toString() {
//    }
//
//    @Test
//    public void toString1() {
//    }

    @Test
    public void computeThreshold_of_zero_elements() {
        RssiCollector rc = new RssiCollector();
        double threshold = rc.computeThreshold(new LinearRewardFunc());
        CustomAssertions.assertEquals(0, threshold);
    }

    @Test
    public void computeThreshold_of_one_element() {
        RssiCollector rc = new RssiCollector();
        rc.enable();
        int element = 92;
        rc.collect(element);

        double threshold = rc.computeThreshold(new LinearRewardFunc());
        CustomAssertions.assertEquals(element, threshold);
    }

    @Test
    public void computeThreshold_of_n_element() {
        RssiCollector rc = new RssiCollector();
        rc.enable();
        int expected_threshold = 73;

        // Go out to the threshold.
        for (int rssi = 20; rssi < expected_threshold; ++rssi) {
            rc.collect(rssi);
        }

        // Stay still at the threshold for a while.
        for (int i = 0; i < 5; ++i) {
            rc.collect(expected_threshold);
        }

        // Come back to the beginning.
        for (int rssi = expected_threshold - 1; rssi >= 20; --rssi) {
            rc.collect(rssi);
        }

        double threshold = rc.computeThreshold(new LinearRewardFunc());
        CustomAssertions.assertEquals(expected_threshold, threshold);
    }
}