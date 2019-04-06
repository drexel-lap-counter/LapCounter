package edu.drexel.lapcounter.lapcounter.backend.ble;

import java.util.List;

/**
 * To calibrate the threshold, the swimmer swims out to the threshold, pauses, and then swims
 * back to the phone. The |RSSI| value to use for the threshold should be the maximum, however
 * we want to minimize noise to avoid false positives.
 *
 * This reward function is a function from (rssi, delta) -> reward. This function should be
 * at a maximum when the RSSI is large and absDelta is 0, and a minimum when absRssi is 0 and
 * absDelta is large.
 *
 * Since Royce and I are not sure what is the best function to use for this, we define this
 * interface so we can swap out functions as needed.
 */
public interface CalibrationRewardFunc {
    double computeReward(int absRssi, int absDelta);
}
