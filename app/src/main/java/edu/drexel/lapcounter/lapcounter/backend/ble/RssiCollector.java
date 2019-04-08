package edu.drexel.lapcounter.lapcounter.backend.ble;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

    private int mMinRssi = Integer.MAX_VALUE;
    private int mMaxRssi = Integer.MIN_VALUE;

    public void collect(int rssi) {
        if (!isEnabled()) {
            Log.w(TAG, "collect() when isEnabled() == false");
            return;
        }

        rssi = Math.abs(rssi);

        mRssiValues.add(rssi);

        if (rssi < mMinRssi) {
            mMinRssi = rssi;
        }

        if (rssi > mMaxRssi) {
            mMaxRssi = rssi;
        }

        log_thread("collect()");
    }

    public boolean isEnabled() {
//        log_thread("isEnabled()");
        return mIsEnabled;
    }

    public void clear() {
        log_thread("clear()");
        mRssiValues.clear();
        mMinRssi = Integer.MAX_VALUE;
        mMaxRssi = Integer.MIN_VALUE;
    }

    public void enable() {
        log_thread("enable()");
        mIsEnabled = true;
    }

    public void disable() {
        log_thread("disable()");
        mIsEnabled = false;
    }

    public double mean() {
        log_thread("mean()");

        if (mRssiValues.isEmpty()) {
            return 0;
        }

        double sum = 0;

        for (double rssi : mRssiValues) {
            sum += rssi;
        }

        return sum / mRssiValues.size();
    }

    public double stdDev(double mean) {
        log_thread("stdDev()");

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
        log_thread("median()");

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
        log_thread("toString()");

        double mean = mean();
        return String.format("n=%d, mean=%.3f, median=%.3f, stdDev=%.3f, min=%d, max=%d",
                mRssiValues.size(), mean, median(), stdDev(mean), min(), max());
    }
}
