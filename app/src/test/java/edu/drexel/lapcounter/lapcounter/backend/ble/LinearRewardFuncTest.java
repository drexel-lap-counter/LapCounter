package edu.drexel.lapcounter.lapcounter.backend.ble;

import org.junit.Test;

import static org.junit.Assert.*;

public class LinearRewardFuncTest {
    //https://floating-point-gui.de/errors/comparison/
    private static boolean nearlyEqual(double a, double b, double epsilon) {
        if (a == b) {
            // shortcut, handles infinities
            return true;
        }

        final double diff = Math.abs(a - b);

        if (a == 0 || b == 0 || diff < Double.MIN_NORMAL) {
            // a or b is zero or both are extremely close to it
            // relative error is less meaningful here
            return diff < (epsilon * Double.MIN_NORMAL);
        }

        final double absA = Math.abs(a);
        final double absB = Math.abs(b);

        // use relative error
        return diff / Math.min((absA + absB), Double.MAX_VALUE) < epsilon;
    }

    private static void assertEquals(double a, double b) {
        assertTrue(nearlyEqual(a, b, 10 * Double.MIN_VALUE));
    }

    @Test
    public void computeReward_zero_rssi_zero_delta() {
        CalibrationRewardFunc f = new LinearRewardFunc();
        double reward = f.computeReward(0, 0);
        assertEquals(100, reward);
    }

    @Test
    public void computeReward_zero_rssi_non_zero_delta() {
        CalibrationRewardFunc f = new LinearRewardFunc();
        int delta = 17;
        double reward = f.computeReward(0, delta);
        assertEquals(100 - delta, reward);
    }

    @Test
    public void computeReward_non_zero_rssi_zero_delta() {
        CalibrationRewardFunc f = new LinearRewardFunc();
        int rssi = 30;
        double reward = f.computeReward(rssi, 0);
        assertEquals(rssi + 100, reward);
    }

    @Test
    public void computeReward_non_zero_rssi_non_zero_delta() {
        CalibrationRewardFunc f = new LinearRewardFunc();
        int rssi = 72;
        int delta = 17;
        double reward = f.computeReward(rssi, delta);
        assertEquals(rssi + 100 - delta , reward);
    }

    private static void testAllWith(LinearRewardFunc f) {
        // We know RSSI is an integer in the domain [20, 120].
        // So let's just test all possible (RSSI, delta) combinations.

        final int minRssi = 20;
        final int maxRssi = 120;
        final int maxDelta = maxRssi - minRssi;

        double rssiWeight = f.getRssiWeight();
        double deltaWeight = f.getDeltaWeight();

        for (int rssi = minRssi; rssi <= maxRssi; ++rssi) {
            for (int delta = 0; delta <= maxDelta; ++delta) {
                double expected = rssi * rssiWeight + deltaWeight * (100 - delta);
                double actual = f.computeReward(rssi, delta);
                assertEquals(expected, actual);
            }
        }
    }

    @Test
    public void computeReward_all_default_weights() {
        testAllWith(new LinearRewardFunc());
    }

    @Test
    public void computeReward_all_double_rssi_weight() {
        testAllWith(new LinearRewardFunc(2.0, 1.0));
    }

    @Test
    public void computeReward_all_double_delta_weight() {
        testAllWith(new LinearRewardFunc(1.0, 2.0));
    }

    @Test
    public void computeReward_all_double_weights() {
        testAllWith(new LinearRewardFunc(2.0, 2.0));
    }

    @Test
    public void computeReward_all_zero_weights() {
        testAllWith(new LinearRewardFunc(0, 0));
    }
}