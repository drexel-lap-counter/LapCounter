package edu.drexel.lapcounter.lapcounter.backend.ble;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A class to store Received Signal Strength Indicator (RSSI) values and to receive useful
 * metrics.
 */
public class RssiCollector {
    private static final String TAG = RssiCollector.class.getSimpleName();

    @SuppressLint("DefaultLocale")
    private void log_thread(String format, Object... args) {
        String s = String.format(format, args);
        s = String.format("[Thread %d] %s", Thread.currentThread().getId(), s);
        Log.d(TAG, s);
    }

    private boolean mIsEnabled = false;
    private final List<Integer> mRssiValues = new ArrayList<>();
    private final List<Integer> mRssiAbsDeltas = new ArrayList<>();

    private int mMinRssi = Integer.MAX_VALUE;
    private int mMaxRssi = Integer.MIN_VALUE;

    /**
     * Add an RSSI value to the internal state.
     * @param rssi The signal strength value to add.
     */
    public void collect(int rssi) {
        if (!isEnabled()) {
            Log.w(TAG, "collect() when isEnabled() == false");
            return;
        }

        // Compute |rssi| and delta from the previous one first before adding things to the
        // list. (computeAbsDelta() assumes that the rssi value has not yet been added to the list)
        rssi = Math.abs(rssi);
        int delta = computeAbsDelta(rssi);

        mRssiValues.add(rssi);
        mRssiAbsDeltas.add(delta);

        updateMinMax(rssi);

        log_thread("collect()");
    }

    /**
     * Compute the absolute delta of the current RSSI value and the previous value
     * @param absRssi the absolute value of the newest RSSI value
     */
    private int computeAbsDelta(int absRssi) {
        if (mRssiValues.size() == 0) {
            return absRssi;
        } else {
            int lastRssi = mRssiValues.get(mRssiValues.size() - 1);
            return Math.abs(absRssi - lastRssi);
        }

    }

    private void updateMinMax(int rssi) {
        if (rssi < mMinRssi) {
            mMinRssi = rssi;
        }

        if (rssi > mMaxRssi) {
            mMaxRssi = rssi;
        }
    }

    /**
     * @return Whether this RssiCollector is accepting more values.
     */
    public boolean isEnabled() {
        return mIsEnabled;
    }


    /**
     * Remove all collected RSSI values and reset calculated metrics.
     */
    public void clear() {
        log_thread("clear()");
        mRssiValues.clear();
        mRssiAbsDeltas.clear();
        mMinRssi = Integer.MAX_VALUE;
        mMaxRssi = Integer.MIN_VALUE;
    }


    /**
     * Instruct the RssiCollector to accept RSSI values.
     */
    public void enable() {
        log_thread("enable()");
        mIsEnabled = true;
    }

    /**
     * Instruct the RssiCollector to stop accepting RSSI values.
     */
    public void disable() {
        log_thread("disable()");
        mIsEnabled = false;
    }

    /**
     * @return The mean of collected RSSI values, or 0 if no RSSI values were collected.
     */
    public double mean() {
        if (mRssiValues.isEmpty()) {
            return 0;
        }

        double sum = 0;

        for (double rssi : mRssiValues) {
            sum += rssi;
        }

        return sum / mRssiValues.size();
    }

    /**
     * @param mean The mean to use when calculating the standard deviation.
     * @return The unbiased sample standard deviation, or 0 if fewer than two RSSI values were
     * collected.
     */
    public double stdDev(double mean) {
        int n = mRssiValues.size();

        if (n <= 1) {
            return 0;
        }

        double sum = 0;

        for (double rssi : mRssiValues) {
            double delta = rssi - mean;
            sum += delta * delta;
        }

        return Math.sqrt(sum / (n - 1));
    }

    public double median() {
        if (mRssiValues.isEmpty()) {
            return 0;
        }

        List<Integer> copy = new ArrayList<>(mRssiValues);
        Collections.sort(copy);

        int n = copy.size();
        int middle_idx =  n / 2;
        double middle = copy.get(middle_idx);

        if (n % 2 == 1) {
            return middle;
        }

        return (copy.get(middle_idx - 1) + middle) / 2.0;
    }

    public int min() {
        return mMinRssi;
    }

    public int max() {
        return mMaxRssi;
    }

    @NonNull
    @SuppressLint("DefaultLocale")
    @Override
    public String toString() {
        double mean = mean();
        return String.format("n=%d, mean=%.3f, median=%.3f, stdDev=%.3f, min=%d, max=%d",
                mRssiValues.size(), mean, median(), stdDev(mean), min(), max());
    }

    @SuppressLint("DefaultLocale")
    public String toString(CalibrationRewardFunc rewardFunc) {
        final BestRewardResult r = computeBestReward(rewardFunc);
        return String.format("%s\nbest_rssi is %.3f with delta of %.3f to yield reward of %.3f",
                             toString(), r.Rssi, r.Delta, r.Reward);
    }

    class BestRewardResult {
        double Rssi;
        double Delta;
        double Reward;

        BestRewardResult(double rssi, double delta, double reward) {
            Rssi = rssi;
            Delta = delta;
            Reward = reward;
        }
    }

    private BestRewardResult computeBestReward(CalibrationRewardFunc rewardFunc) {
        double maxReward = Double.NEGATIVE_INFINITY;
        double bestRSSI = 0.0;
        double bestDelta = Double.POSITIVE_INFINITY;

        for (int i = 0; i < mRssiValues.size(); i++) {
            int rssi = mRssiValues.get(i);
            int delta = mRssiAbsDeltas.get(i);
            double reward = rewardFunc.computeReward(rssi, delta);

            if (reward > maxReward) {
                maxReward = reward;
                bestRSSI = rssi;
                bestDelta = delta;
            }
        }

        return new BestRewardResult(bestRSSI, bestDelta, maxReward);
    }

    /**
     * Once we are done collecting RSSI values and deltas, determine which has the maximum
     * RSSI value while minimizing noise. This is done by iterating over the list of collected
     * values/deltas, computing a reward function for each pair (rssi, delta), and take the
     * RSSI with the maximum reward function value.
     *
     * @param rewardFunc the function to maximize. This is an interface so we can experiment
     *                   with different functions.
     * @return the rssi with the highest reward
     */
    public double computeThreshold(CalibrationRewardFunc rewardFunc) {
        return computeBestReward(rewardFunc).Rssi;
    }

    public class ListSizes {
        int NumValues;
        int NumDeltas;

        ListSizes(int numValues, int numDeltas) {
            NumValues = numValues;
            NumDeltas = numDeltas;
        }
    }

    public ListSizes getListSizes() {
        return new ListSizes(mRssiValues.size(), mRssiAbsDeltas.size());
    }
}
