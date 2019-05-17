package edu.drexel.lapcounter.lapcounter.backend.ble;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;

import edu.drexel.lapcounter.lapcounter.backend.Hyperparameters;
import edu.drexel.lapcounter.lapcounter.backend.SimpleMessageReceiver;
import edu.drexel.lapcounter.lapcounter.backend.wrappers.LocalBroadcastManagerWrapper;

/**
 * A class that schedules and receives raw RSSI values from a connected device and publishes
 * Intents containing filtered RSSI values and direction information.
 */
public class RSSIManager {
    /**
     * @param s the identifier to prepend with the qualified package name.
     * @return the identifier fully-qualified with package information.
     */
    private static String qualify(String s) {
        return RSSIManager.class.getPackage().getName() + "." + s;
    }

    /**
     * The action of the Intent that RSSIManager publishes for a filtered RSSI value and direction.
     * This information is contained in the {@link #EXTRA_RSSI} and {@link #EXTRA_DIRECTION} extras
     * of the published intent.
     *
     * @see #publishRssiAndDirection()
     */
    public final static String ACTION_RSSI_AND_DIR_AVAILABLE =
            qualify("ACTION_RSSI_AND_DIR_AVAILABLE");

    /**
     * The extra of the Intent with action {@link #ACTION_RSSI_AND_DIR_AVAILABLE} containing the
     * filtered RSSI value.
     */
    public final static String EXTRA_RSSI = qualify("EXTRA_RSSI");

    /**
     * The extra of the Intent with action {@link #ACTION_RSSI_AND_DIR_AVAILABLE} containing the
     * direction information.
     *
     * @see #DIRECTION_OUT
     * @see #DIRECTION_IN
     * @see #getDirection()
     */
    public final static String EXTRA_DIRECTION = qualify("EXTRA_DIRECTION");

    /**
     * The direction of increasing absolute RSSI values.
     *
     * @see #EXTRA_DIRECTION
     */
    public static final int DIRECTION_OUT = 1;


    /**
     * The direction of decreasing absolute RSSI values.
     *
     * @see #EXTRA_DIRECTION
     */
    public static final int DIRECTION_IN = -1;

    /**
     * An Intent-publishing manager to send Intents across Activities and Services.
     */
    private IBroadcastManager mBroadcastManager;


    /**
     * The default sliding window size of {@link #mRssiDeltas}.
     */
    public static final int DEFAULT_DELTAS_WINDOW_SIZE = Hyperparameters.RSSI_DELTA_WINDOW_SIZE;

    /**
     * A sliding-window containing the differences between consecutive pairs of filtered RSSI
     * values.
     */
    private SlidingWindow<Double> mRssiDeltas = new SlidingWindow<>(DEFAULT_DELTAS_WINDOW_SIZE);

    /**
     * The default moving average size of {@link #mFilter}.
     */
    public static final int DEFAULT_MOVING_AVERAGE_SIZE = Hyperparameters.RSSI_FILTER_WINDOW_SIZE;

    /**
     * A moving average of raw RSSI values used to smooth out noise.
     */
    private MovingAverage mFilter = new MovingAverage(DEFAULT_MOVING_AVERAGE_SIZE);

    /**
     * The previous filtered RSSI value. Used to compute a delta for {@link #mRssiDeltas}.
     */
    private double mPreviousRssi;

    /**
     * The most recent filtered RSSI value.
     *
     * @see #mPreviousRssi
     * @see #publishRssiAndDirection()
     */
    private double mCurrentRssi;


    /**
     * How often in milliseconds to request an RSSI value when {@link #mRssiDeltas} and
     * {@link #mFilter} are filled.
     *
     * @see #scheduleRssiRequest()
     */
    private static final int NORMAL_RSSI_PERIOD_MS = Hyperparameters.RSSI_POLL_PERIOD_MS;

    /**
     * How often in milliseconds to request an RSSI value when {@link #mRssiDeltas} and
     * {@link #mFilter} are not filled.
     *
     * @see #NORMAL_RSSI_PERIOD_MS
     * @see #onConnect
     */
    private static final int RECONNECT_RSSI_PERIOD_MS =
            Hyperparameters.RECONNECT_RSSI_POLL_PERIOD_MS;


    /**
     * @return the current RSSI poll frequency in milliseconds
     *
     * @see #NORMAL_RSSI_PERIOD_MS
     * @see #RECONNECT_RSSI_PERIOD_MS
     */
    public int getPollFrequencyMs() {
        return mPollFrequencyMs;
    }

    /**
     * The current RSSI poll frequency in milliseconds. The default frequency when the class is
     * first constructed is {@link #RECONNECT_RSSI_PERIOD_MS}.
     *
     * @see #getPollFrequencyMs()
     * @see #scheduleRssiRequest()
     */
    private int mPollFrequencyMs = RECONNECT_RSSI_PERIOD_MS;

    /**
     * The interface to Android's Bluetooth Low Energy stack through which this class requests RSSI
     * values.
     *
     * @see #scheduleRssiRequest()
     */
    private final BLEComm mBleComm;

    /**
     * An asynchronous handler to execute delayed RSSI requests.
     */
    private final Handler mHandler = new Handler();


    /**
     * Whether this class should schedule another RSSI request.
     *
     * @see #scheduleRssiRequest()
     */
    private boolean mShouldScheduleRssi = false;


    /**
     * The callback that receives raw RSSI values from {@link #mBleComm}.
     * These values are filtered using {@link #mFilter}, and, if {@link #windowsAreFull()},
     * publishes an Intent containing the filtered RSSI value and direction information.
     *
     * @see BLEComm#ACTION_RAW_RSSI_AVAILABLE
     * @see #publishRssiAndDirection()
     */
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

    /**
     * The callback invoked when a device has connected. Sets the polling frequency to
     * {@link #RECONNECT_RSSI_PERIOD_MS} and schedules an RSSI request if
     * {@link #mShouldScheduleRssi}.
     */
    private SimpleMessageReceiver.MessageHandler onConnect = new SimpleMessageReceiver.MessageHandler() {
        @Override
        public void onMessage(Intent message) {
            mPollFrequencyMs = RECONNECT_RSSI_PERIOD_MS;

            if (mShouldScheduleRssi) {
                scheduleRssiRequest();
            }
        }
    };


    /**
     * The callback invoked when a device has disconnected. Calls {@link #clear()} and
     * cancels all pending RSSI requests.
     */
    private SimpleMessageReceiver.MessageHandler onDisconnect = new SimpleMessageReceiver.MessageHandler() {
        @Override
        public void onMessage(Intent message) {
            clear();
            mHandler.removeCallbacks(mRequestRssi);
        }
    };

    /**
     * The request RSSI task that {@link #mHandler} executes every {@link #mPollFrequencyMs}.
     * This task returns early if no device is connected.
     * This task automatically schedules another RSSI request through
     * {@link #scheduleRssiRequest()}.
     */
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


    /**
     * Schedule an RSSI request every {@link #mPollFrequencyMs}.
     * This method will cancel any pending RSSI requests prior to the method's invocation.
     */
    public void scheduleRssiRequest() {
        stopRssiRequests();
        mShouldScheduleRssi = true;
        mHandler.postDelayed(mRequestRssi, mPollFrequencyMs);
    }

    /**
     * Construct an instance of this class.
     *
     * @param context this class requires a Context to send and receive Intents.
     * @param bleComm the interface to Android's Bluetooth Low Energy stack through which this
     *                class requests RSSI values.
     */
    public RSSIManager(Context context, BLEComm bleComm) {
        this(LocalBroadcastManagerWrapper.getInstance(context), bleComm);
    }

    /**
     * Construct an instance of this class.
     *
     * @param broadcastManager an Intent-publishing manager to send Intents across Activities and
     *                         Services.
     * @param bleComm the interface to Android's Bluetooth Low Energy stack through which this
     *                class requests RSSI values.
     */
    public RSSIManager(IBroadcastManager broadcastManager, BLEComm bleComm) {
        mBroadcastManager = broadcastManager;
        mBleComm = bleComm;
    }

    /**
     * Register callbacks to Intents that this class requires.
     *
     * @param receiver the receiver through which this class registers required Intents.
     *
     * @see #onConnect
     * @see #onDisconnect
     * @see #onRawRssi
     */
    public void initCallbacks(SimpleMessageReceiver receiver) {
        receiver.registerHandler(BLEComm.ACTION_CONNECTED, onConnect);
        receiver.registerHandler(BLEComm.ACTION_RECONNECTED, onConnect);
        receiver.registerHandler(BLEComm.ACTION_DISCONNECTED, onDisconnect);
        receiver.registerHandler(BLEComm.ACTION_RAW_RSSI_AVAILABLE, onRawRssi);
    }

    /**
     * @return the most current filtered RSSI value
     * @see #mCurrentRssi
     * @see #mFilter
     */
    public double getRssi() {
        return mCurrentRssi;
    }

    /**
     * @return the overall direction of the RSSI deltas
     *
     * @see #mRssiDeltas
     * @see #DIRECTION_OUT
     * @see #DIRECTION_IN
     * @see #EXTRA_DIRECTION
     */
    public int getDirection() {
        double sum = 0.0;
        for (double dx : mRssiDeltas) {
            sum += dx;
        }
        return (int)Math.signum(sum);
    }

    /**
     * Publish the current filtered RSSI value and the direction information.
     *
     * @see #ACTION_RSSI_AND_DIR_AVAILABLE
     * @see #EXTRA_RSSI
     * @see #EXTRA_DIRECTION
     * @see #mCurrentRssi
     * @see #getDirection()
     */
    private void publishRssiAndDirection() {
        Intent intent = new Intent(ACTION_RSSI_AND_DIR_AVAILABLE);
        intent.putExtra(EXTRA_RSSI, mCurrentRssi);
        intent.putExtra(EXTRA_DIRECTION, getDirection());
        mBroadcastManager.sendBroadcast(intent);
    }

    /**
     * Insert a delta to {@link #mRssiDeltas} using the current filtered RSSI value and the previous
     * filtered RSSI value.
     * @see #mCurrentRssi
     * @see #mPreviousRssi
     */
    private void updateDeltas() {
        mRssiDeltas.addLast(mCurrentRssi - mPreviousRssi);
    }

    /**
     * @return whether {@link #mFilter} and {@link #mRssiDeltas} are filled
     *
     * @see #onRawRssi
     */
    public boolean windowsAreFull() {
        return mFilter.windowIsFull() && mRssiDeltas.isFull();
    }

    /**
     * Clear {@link #mFilter} and {@link #mRssiDeltas}.
     */
    public void clear() {
        mRssiDeltas.clear();
        mFilter.clear();
    }

    /**
     * Cancel all pending RSSI requests.
     */
    public void stopRssiRequests() {
        mShouldScheduleRssi = false;
        mHandler.removeCallbacks(mRequestRssi);
    }

    /**
     * Set the size of {@link #mRssiDeltas}.
     *
     * Smaller values cause the direction information to be
     * susceptible to noise. Larger values cause direction to take on more inertia, thus requiring
     * more time to reflect dramatic changes in direction.
     * @param size the new sliding window size
     */
    public void setDeltasSize(int size) {
        mRssiDeltas = new SlidingWindow<>(size);
    }


    /**
     * Set the size of {@link #mFilter}.
     *
     * Smaller values lead to averages of fewer values, making
     * the filter susceptible to noise. Larger values lead to averages of more values, causing
     * the filter to require more time to reflect dramatic changes in collected RSSI values.
     *
     * @param size the new moving average size.
     */
    public void setFilterSize(int size) {
        mFilter = new MovingAverage(size);
    }
}
