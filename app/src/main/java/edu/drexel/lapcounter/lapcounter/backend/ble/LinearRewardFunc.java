package edu.drexel.lapcounter.lapcounter.backend.ble;

/**
 * Peter's idea for the simplest possible calibration reward function.
 *
 * the rssi value is maximized, but there is a second reward term of (100 - delta). This term
 * gets maximized when the delta is 0 (very stable) and gets smaller as delta increases
 */
public class LinearRewardFunc implements CalibrationRewardFunc {
    private double mRssiWeight;
    private double mDeltaWeight;

    public LinearRewardFunc() {
        this(1.0, 1.0);
    }

    public LinearRewardFunc(double rssiWeight, double deltaWeight) {
        mRssiWeight = rssiWeight;
        mDeltaWeight = deltaWeight;
    }

    @Override
    public double computeReward(int absRssi, int absDelta) {
        return mRssiWeight * absRssi + mDeltaWeight * (100 - absDelta);
    }

    public double getRssiWeight() {
        return mRssiWeight;
    }

    public double getDeltaWeight() {
        return mDeltaWeight;
    }
}
