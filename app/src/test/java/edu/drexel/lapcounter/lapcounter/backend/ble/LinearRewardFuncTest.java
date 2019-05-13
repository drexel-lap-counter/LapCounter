package edu.drexel.lapcounter.lapcounter.backend.ble;

import android.util.CustomAssertions;

import org.junit.Test;

public class LinearRewardFuncTest {
    @Test
    public void computeReward_zero_rssi_zero_delta() {
        CalibrationRewardFunc f = new LinearRewardFunc();
        double reward = f.computeReward(0, 0);
        CustomAssertions.assertEquals(100, reward);
    }

    @Test
    public void computeReward_zero_rssi_non_zero_delta() {
        CalibrationRewardFunc f = new LinearRewardFunc();
        int delta = 17;
        double reward = f.computeReward(0, delta);
        CustomAssertions.assertEquals(100 - delta, reward);
    }

    @Test
    public void computeReward_non_zero_rssi_zero_delta() {
        CalibrationRewardFunc f = new LinearRewardFunc();
        int rssi = 30;
        double reward = f.computeReward(rssi, 0);
        CustomAssertions.assertEquals(rssi + 100, reward);
    }

    @Test
    public void computeReward_non_zero_rssi_non_zero_delta() {
        CalibrationRewardFunc f = new LinearRewardFunc();
        int rssi = 72;
        int delta = 17;
        double reward = f.computeReward(rssi, delta);
        CustomAssertions.assertEquals(rssi + 100 - delta , reward);
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
                CustomAssertions.assertEquals(expected, actual);
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