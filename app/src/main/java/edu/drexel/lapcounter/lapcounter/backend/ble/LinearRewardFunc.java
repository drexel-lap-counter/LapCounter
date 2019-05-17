package edu.drexel.lapcounter.lapcounter.backend.ble;

/**
 * Peter's idea for the simplest possible calibration reward function.
 *
 * the rssi value is maximized, but there is a second reward term of (100 - |delta|). This term
 * gets maximized when the delta is 0 (very stable) and gets smaller as delta increases
 *
 * The overall formula is
 *
 * reward = rssi_weight * |RSSI| + delta_weight * (100 - |delta|)
 */
public class LinearRewardFunc implements CalibrationRewardFunc {
    /**
     * How much to weight the RSSI in the calculation
     */
    private double mRssiWeight;
    /**
     * How much to weight the delta part of the calculationn
     */
    private double mDeltaWeight;

    /**
     * Create the simplest reward function, where the weights are equal
     */
    public LinearRewardFunc() {
        this(1.0, 1.0);
    }

    /**
     * Create a reward function with arbitrary weights.
     * @param rssiWeight the weight for the RSSI part of the formula
     * @param deltaWeight the weight for the absolute delta RSSI for the formula
     */
    public LinearRewardFunc(double rssiWeight, double deltaWeight) {
        mRssiWeight = rssiWeight;
        mDeltaWeight = deltaWeight;
    }

    @Override
    public double computeReward(int absRssi, int absDelta) {
        return mRssiWeight * absRssi + mDeltaWeight * (100 - absDelta);
    }

    /**
     * Get the current weight for the RSSI value
     * @return the weight for this part of the formula
     */
    public double getRssiWeight() {
        return mRssiWeight;
    }

    /**
     * Get the current weight for the delta component
     * @return the weight for this part of the formula
     */
    public double getDeltaWeight() {
        return mDeltaWeight;
    }
}
