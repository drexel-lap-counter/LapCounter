package edu.drexel.lapcounter.lapcounter.backend.ble;

/**
 * Royce's idea for an exponential reward function, with a minor change to make sure the
 * denominator doesn't go to 0.
 */
public class ExponentialRewardFunc implements CalibrationRewardFunc {
    private double mRssiExponent;
    private double mDeltaExponent;

    public ExponentialRewardFunc() {
        this(1.0, 1.0);
    }

    public ExponentialRewardFunc(double rssiExp, double deltaExp) {
        mRssiExponent = rssiExp;
        mDeltaExponent = deltaExp;
    }

    @Override
    public double computeReward(int absRssi, int absDelta) {
        double numerator = Math.pow(absRssi, mRssiExponent);
        // the + 1 is to ensure the function stays bounded when delta is 0
        double denominator = Math.pow(absDelta + 1.0, mDeltaExponent);

        return numerator / denominator;
    }
}
