package edu.drexel.lapcounter.lapcounter.backend.ble;


/**
 * Compute a moving average of N values. This is used as a low-pass filter on the RSSI values.
 */
public class MovingAverage {
    /**
     * The last N values seen
     */
    private final SlidingWindow<Double> mValues;

    /**
     * Create the averaging element with a given size N
     * @param maxSize Specify how many elements to store at a time.
     */
    public MovingAverage(int maxSize) {
        mValues = new SlidingWindow<>(maxSize);
    }


    /**
     * Check if the sliding window is full. This means we have a valid average value
     * @return true if the buffer is full.
     */
    public boolean windowIsFull() {
        return mValues.isFull();
    }

    /**
     * Average points in memory. If there are no points,
     * return 0 instead. Note that this will not be entirely accurate
     * until there are N points in memory.
     * @return the current average of the most recent points.
     */
    private double computeAverage() {
        // If we have no values, just return to avoid dividing by 0.
        if (mValues.size() == 0)
            return 0.0;

        // Sum up the current values
        double sum = 0.0;
        for (double x : mValues)
            sum += x;

        // return the average
        return sum / mValues.size();
    }

    /**
     * Add a value to the moving average filter
     * @param value the new value to add to thee buffer
     * @return the new moving average
     */
    public double filter(double value) {
        mValues.addLast(value);
        return computeAverage();
    }

    /***
     * Clear the underlying buffer.
     */
    public void clear() {
        mValues.clear();
    }
}
