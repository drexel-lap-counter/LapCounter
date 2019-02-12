package edu.drexel.lapcounter.lapcounter.backend;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

/**
 * Lightweight class for tracking the lap count number. It subscribes to events to determine
 * when to increment the counter. It also publishes an event whenever the counter changes.
 */
public class LapCounter {

    // IDs for intents
    public static final String ACTION_LAP_COUNT_UPDATED =
            "edu.drexel.lapcounter.lapcounter.ACTION_LAP_COUNT_UPDATED";
    public static final String EXTRA_LAP_COUNT =
            "edu.drexel.lapcounter.lapcounter.EXTRA_LAP_COUNT";

    /**
     * Every time a lap is counted, add this value to the lap count.
     * A lap is defined as one length of the
     */
    private static final int LAP_INCREMENT = 2;

    /**
     * Current lap count.
     */
    private int mLapCount = 0;

    /**
     * Parent LapCounter Service
     */
    private Context mContext;

    /**
     * Listen for the following events:
     *
     * STATE_TRANSITION -> if we get a state transition from FAR -> NEAR, count laps
     *
     * RECONNECT_LAPS -> If we reconnected and it is determined that the athlete went from
     *      FAR -> NEAR in the meantime, count laps
     */
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            switch (action) {
                case LocationStateMachine.ACTION_STATE_TRANSITION:
                    countLaps(intent);
                    break;
                case DisconnectManager.ACTION_MISSED_LAPS:
                    // this message is only published when the DisconnectManager
                    // detects laps missed
                    incrementCounter();
                    break;
            }
        }

    };

    public LapCounter(Context context) {
        mContext = context;
    }

    /**
     * Count laps in the normal case: a state transition. This method filters for
     * FAR -> NEAR transitions only
     * @param intent the received message
     */
    private void countLaps(Intent intent) {
        // TODO: Check the intent's extras for the before and after states
        // on Far -> Near increment mLapCount;

        // TODO: Only call this if a lap is counted.
        incrementCounter();
    }

    private void incrementCounter() {
        mLapCount += LAP_INCREMENT;
        publishLapCount();
    }

    /**
     * Broadcast that the lap count changed
     */
    private void publishLapCount() {
        final Intent intent = new Intent(ACTION_LAP_COUNT_UPDATED);
        intent.putExtra(EXTRA_LAP_COUNT, mLapCount);
        mContext.sendBroadcast(intent);
    }

    /**
     * Call this to initialize callbacks
     */
    public void registerReceiver() {
        mContext.registerReceiver(mReceiver, makeIntentFilter());
    }

    /**
     * Call this to stop receiving events
     */
    public void unregisterReceiver() {
        mContext.unregisterReceiver(mReceiver);
    }

    /**
     * This is needed to declare which intents we care about.
     * @return
     */
    private static IntentFilter makeIntentFilter() {
        final IntentFilter filter = new IntentFilter();
        filter.addAction(LocationStateMachine.ACTION_STATE_TRANSITION);
        filter.addAction(DisconnectManager.ACTION_MISSED_LAPS);
        return filter;
    }


}
