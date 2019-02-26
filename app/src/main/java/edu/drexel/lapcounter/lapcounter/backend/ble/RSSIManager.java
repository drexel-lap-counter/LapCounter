package edu.drexel.lapcounter.lapcounter.backend.ble;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;

import edu.drexel.lapcounter.lapcounter.backend.SimpleMessageReceiver;

public class RSSIManager {
    private static String qualify(String s) {
        return RSSIManager.class.getPackage().getName() + "." + s;
    }

    public final static String ACTION_RSSI_AND_DIR_AVAILABLE =
            qualify("ACTION_RSSI_AND_DIR_AVAILABLE");

    public final static String EXTRA_RSSI = qualify("EXTRA_RSSI");
    public final static String EXTRA_DIRECTION = qualify("EXTRA_DIRECTION");

    public static final int DIRECTION_OUT = 1;
    public static final int DIRECTION_IN = -1;

    private LocalBroadcastManager mBroadcastManager;

    private static final int DELTAS_WINDOW_SIZE = 3;
    private final SlidingWindow<Double> mRssiDeltas = new SlidingWindow<>(DELTAS_WINDOW_SIZE);

    private static final int MOVING_AVERAGE_SIZE = 10;
    private final MovingAverage mFilter = new MovingAverage(MOVING_AVERAGE_SIZE);

    private double mPreviousRssi;
    private double mCurrentRssi;

    private static final int NORMAL_RSSI_PERIOD_MS = 300;
    private static final int RECONNECT_RSSI_PERIOD_MS = 100;

    private int mPollFrequencyMs = RECONNECT_RSSI_PERIOD_MS;

    private final BLEComm mBleComm;

    private final Handler mHandler = new Handler();

    private boolean mShouldScheduleRssi = false;

    private SimpleMessageReceiver.MessageHandler onRawRssi = new SimpleMessageReceiver.MessageHandler() {
        @Override
        public void onMessage(Intent message) {
            int rssi = Math.abs(message.getIntExtra(BLEComm.EXTRA_RAW_RSSI, 0));

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

    private SimpleMessageReceiver.MessageHandler onConnect = new SimpleMessageReceiver.MessageHandler() {
        @Override
        public void onMessage(Intent message) {
            mPollFrequencyMs = RECONNECT_RSSI_PERIOD_MS;

            if (mShouldScheduleRssi) {
                scheduleRssiRequest();
            }
        }
    };


    private SimpleMessageReceiver.MessageHandler onDisconnect = new SimpleMessageReceiver.MessageHandler() {
        @Override
        public void onMessage(Intent message) {
            clear();
            mHandler.removeCallbacks(mRequestRssi);
        }
    };

    private final Runnable mRequestRssi = new Runnable() {
        @Override
        public void run() {
            if (mBleComm.getConnectionState() != BLEComm.STATE_CONNECTED) {
                return;
            }

            mBleComm.requestRssi();
            scheduleRssiRequest();
        }
    };

    public void scheduleRssiRequest() {
        stopRssiRequests();
        mShouldScheduleRssi = true;
        mHandler.postDelayed(mRequestRssi, mPollFrequencyMs);
    }

    public RSSIManager(Context context, BLEComm bleComm) {
        mBroadcastManager = LocalBroadcastManager.getInstance(context);
        mBleComm = bleComm;
    }

    public void initCallbacks(SimpleMessageReceiver receiver) {
        receiver.registerHandler(BLEComm.ACTION_CONNECTED, onConnect);
        receiver.registerHandler(BLEComm.ACTION_DISCONNECTED, onDisconnect);
        receiver.registerHandler(BLEComm.ACTION_RAW_RSSI_AVAILABLE, onRawRssi);
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

    public void clear() {
        mRssiDeltas.clear();
        mFilter.clear();
    }

    public void stopRssiRequests() {
        mShouldScheduleRssi = false;
        mHandler.removeCallbacks(mRequestRssi);
    }
}
