package edu.drexel.lapcounter.lapcounter.backend;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

public class RSSIManager {
    public final static String ACTION_RSSI_AND_DIR_AVAILABLE =
            "edu.drexel.lapcounter.lapcounter.backend.ACTION_RSSI_AND_DIR_AVAILABLE";

    public final static String EXTRA_RSSI = "edu.drexel.lapcounter.lapcounter.backend.EXTRA_RSSI";
    public final static String EXTRA_DIRECTION =
            "edu.drexel.lapcounter.lapcounter.backend.EXTRA_DIRECTION";

    public static final int DIRECTION_OUT = 1;
    public static final int DIRECTION_IN = -1;

    private LocalBroadcastManager mBroadcastManager;

    private static final int DELTAS_WINDOW_SIZE = 3;
    private final SlidingWindow<Double> mRssiDeltas = new SlidingWindow<>(DELTAS_WINDOW_SIZE);

    private static final int MOVING_AVERAGE_SIZE = 10;
    private final MovingAverage mFilter = new MovingAverage(MOVING_AVERAGE_SIZE);

    private double mPreviousRssi;
    private double mCurrentRssi;

    // TODO: Schedule RSSI Request.
    private static final int NORMAL_RSSI_PERIOD_MS = 300;
    private int mPollFrequencyMs = NORMAL_RSSI_PERIOD_MS;

    private SimpleMessageReceiver.MessageHandler onRawRssi = new SimpleMessageReceiver.MessageHandler() {
        @Override
        public void onMessage(Intent message) {
            int rssi = Math.abs(message.getIntExtra(BLEService.EXTRA_RAW_RSSI, 0));

            if (rssi == 0) {
                return;
            }

            mPreviousRssi = mCurrentRssi;
            mCurrentRssi = mFilter.filter(rssi);
            updateDeltas();

            if (windowsAreFull()) {
                mPollFrequencyMs = NORMAL_RSSI_PERIOD_MS;
                publishRssiAndDirection();
            }
        }
    };


    public RSSIManager(Context context) {
        mBroadcastManager = LocalBroadcastManager.getInstance(context);
    }

    public void initCallbacks(SimpleMessageReceiver receiver) {
        receiver.registerHandler(BLEService.ACTION_RAW_RSSI_AVAILABLE, onRawRssi);
    }

    public double getRssi() {
        return mCurrentRssi;
    }

    public int getDirection() {
        double sum = 0.0;
        for (double dx : mRssiDeltas) {
            sum += dx;
        }
        return (int)Math.signum(sum);
    }

    private void publishRssiAndDirection() {
        Intent intent = new Intent(ACTION_RSSI_AND_DIR_AVAILABLE);
        intent.putExtra(EXTRA_RSSI, mCurrentRssi);
        intent.putExtra(EXTRA_DIRECTION, getDirection());
        mBroadcastManager.sendBroadcast(intent);
    }

    private void updateDeltas() {
        mRssiDeltas.addLast(mCurrentRssi - mPreviousRssi);
    }

    private boolean windowsAreFull() {
        return mFilter.windowIsFull() && mRssiDeltas.isFull();
    }

    public void setPollFrequency(int frequencyMs) {
        mPollFrequencyMs = frequencyMs;
    }

    public void clear() {
        mRssiDeltas.clear();
        mFilter.clear();
    }
}
