package edu.drexel.lapcounter.lapcounter.backend.ble;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A class to store Received Signal Strength Indicator (RSSI) values and to provide useful
 * metrics on them.
 */
public class RssiCollector {
    /**
     * The tag used to identify this class in execution logs.
     */
    private static final String TAG = RssiCollector.class.getSimpleName();

    /** Logs a message with the current thread ID using {@link String#format(String, Object...)}.
     * @param format the string format passed to {@link String#format(String, Object...)}.
     * @param args the object arguments passed to {@link String#format(String, Object...)}.
     */
    @SuppressLint("DefaultLocale")
    private void log_thread(String format, Object... args) {
        String s = String.format(format, args);
        s = String.format("[Thread %d] %s", Thread.currentThread().getId(), s);
        Log.d(TAG, s);
    }

    /**
     * Whether this RssiCollector is accepting RSSI values.
     * @see #isEnabled()
     * @see #collect(int)
     */
    private boolean mIsEnabled = false;

    /**
     * The collection of the absolute value of raw RSSI values so far received.
     */
    private final List<Integer> mRssiValues = new ArrayList<>();

    /**
     * The absolute differences between consecutive pairs of RSSI values in {@link #mRssiValues}.
     * @see #computeAbsDelta(int)
     */
    private final List<Integer> mRssiAbsDeltas = new ArrayList<>();

    /**
     * The smallest absolute RSSI value so far received or {@link Integer#MAX_VALUE} if no values
     * have been received.
     * @see #min()
     * @see #updateMinMax(int)
     */
    private int mMinRssi = Integer.MAX_VALUE;

    /**
     * The largest absolute RSSI value so far received or {@link Integer#MIN_VALUE} if no values
     * have been received.
     * @see #max()
     * @see #updateMinMax(int)
     */
    private int mMaxRssi = Integer.MIN_VALUE;

    /**
     * Add an RSSI value to the internal state.
     * @param rssi the signal strength value to add.
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

    /** Update, if appropriate, {@link #mMinRssi} or {@link #mMaxRssi} with the provided RSSI value.
     * @param rssi the RSSI value that may update the min or max
     * @see #min()
     * @see #max()
     */
    private void updateMinMax(int rssi) {
        if (rssi < mMinRssi) {
            mMinRssi = rssi;
        }

        if (rssi > mMaxRssi) {
            mMaxRssi = rssi;
        }
    }

    /**
     * @return whether this RssiCollector is accepting more values.
     * @see #mIsEnabled
     * @see #collect(int)
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
     * @return the mean of collected RSSI values, or 0 if no RSSI values were collected.
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
     * @param mean the mean to use when calculating the standard deviation.
     * @return the unbiased sample standard deviation, or 0 if fewer than two RSSI values were
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

    /**
     * @return the median of collected RSSI values, or 0 if no values have been collected.
     */
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

    /**
     * @return the minimum absolute RSSI value so far received.
     * @see #mMinRssi
     * @see #updateMinMax(int)
     */
    public int min() {
        return mMinRssi;
    }

    /**
     * @return the maximum absolute RSSI value so far received.
     * @see #mMaxRssi
     * @see #updateMinMax(int)
     */
    public int max() {
        return mMaxRssi;
    }

    /**
     * @return a debug-friendly formatting of this RssiCollector containing the number of RSSI
     * values collected and other metrics.
     * @see #mean()
     * @see #median()
     * @see #stdDev(double)
     * @see #min()
     * @see #max()
     */
    @NonNull
    @SuppressLint("DefaultLocale")
    @Override
    public String toString() {
        double mean = mean();
        return String.format("n=%d, mean=%.3f, median=%.3f, stdDev=%.3f, min=%d, max=%d",
                mRssiValues.size(), mean, median(), stdDev(mean), min(), max());
    }


    /**
     * @param rewardFunc the reward function to use for calibration reward information.
     * @return a debug-friendly formatting of this RssiCollector containing the number of RSSI
     * values collected and other metrics, along with calibration reward information.
     * @see #toString()
     * @see #computeBestReward(CalibrationRewardFunc)
     */
    @SuppressLint("DefaultLocale")
    public String toString(CalibrationRewardFunc rewardFunc) {
        final BestRewardResult r = computeBestReward(rewardFunc);
        return String.format("%s\nbest_rssi is %.3f with delta of %.3f to yield reward of %.3f",
                             toString(), r.Rssi, r.Delta, r.Reward);
    }

    /**
     * A struct containing information about the best calibration reward of this RssiCollector's
     * RSSI values.
     */
    class BestRewardResult {
        /**
         * The RSSI value at which the best reward was found.
         */
        double Rssi;

        /**
         * The delta between {@link #Rssi} and the previous received RSSI value.
         */
        double Delta;


        /**
         * The reward calculated from some {@link CalibrationRewardFunc}.
         */
        double Reward;

        /**
         * Construct a BestRewardResult with the information about the best reward.
         *
         * @param rssi the RSSI value at which the best reward was found.
         * @param delta the delta between {@link #Rssi} and the previous received RSSI value.
         * @param reward the reward calculated from some {@link CalibrationRewardFunc}.
         */
        BestRewardResult(double rssi, double delta, double reward) {
            Rssi = rssi;
            Delta = delta;
            Reward = reward;
        }
    }

    /**
     * @param rewardFunc the reward function to maximize
     * @return information about the best reward of this RssiCollector's RSSI values
     */
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


    /**
     * A tuple to hold size information about {@link #mRssiValues} and {@link #mRssiAbsDeltas}.
     */
    public class ListSizes {

        /**
         * Number of items in {@link #mRssiValues}.
         */
        int NumValues;


        /**
         * Number of items in {@link #mRssiAbsDeltas}.
         */
        int NumDeltas;

        /**
         * Construct a ListSizes object with the given sizes of {@link #mRssiValues} and
         * {@link #mRssiAbsDeltas}.
         *
         * @param numValues number of items in {@link #mRssiValues}.
         * @param numDeltas number of items in {@link #mRssiAbsDeltas}.
         */
        ListSizes(int numValues, int numDeltas) {
            NumValues = numValues;
            NumDeltas = numDeltas;
        }
    }

    /**
     * @return a tuple containing the size of {@link #mRssiValues} and {@link #mRssiAbsDeltas}.
     */
    public ListSizes getListSizes() {
        return new ListSizes(mRssiValues.size(), mRssiAbsDeltas.size());
    }
}
