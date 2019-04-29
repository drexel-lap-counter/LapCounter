package edu.drexel.lapcounter.lapcounter.backend.lapcounter;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import edu.drexel.lapcounter.lapcounter.backend.SimpleMessageReceiver;

import static edu.drexel.lapcounter.lapcounter.backend.SimpleMessageReceiver.MessageHandler;
import static edu.drexel.lapcounter.lapcounter.backend.lapcounter.LocationStateMachine.EXTRA_STATE_AFTER;
import static edu.drexel.lapcounter.lapcounter.backend.lapcounter.LocationStateMachine.EXTRA_STATE_BEFORE;

/**
 * Lightweight class for tracking the lap count number. It subscribes to events to determine
 * when to increment the counter. It also publishes an event whenever the counter changes.
 */
public class LapCounter {
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
     * Used to publish events.
     */
    private LocalBroadcastManager mBroadcastManager;

    /**
     * Count laps in the normal case: a state transition. This method filters for
     * FAR -> NEAR transitions only
     * @param intent the received message
     */
    private MessageHandler countLaps = new MessageHandler() {
        @Override
        public void onMessage(Intent message) {
            LocationStateMachine.State before = (LocationStateMachine.State) message.getSerializableExtra(EXTRA_STATE_BEFORE);
            LocationStateMachine.State after = (LocationStateMachine.State) message.getSerializableExtra(EXTRA_STATE_AFTER);

            // on Far -> Near increment mLapCount;
            if (before == LocationStateMachine.State.FAR && after == LocationStateMachine.State.NEAR) {
                incrementCounter();
            }
        }
    };

    /**
     * Every Missed Laps events corresponds to 1 counter increment.
     */
    private MessageHandler onMissedLaps = new MessageHandler() {
        @Override
        public void onMessage(Intent message) {
            incrementCounter();
        }
    };

    /**
     * Constructor
     * @param context the parent Service for setting up callbacks
     */
    public LapCounter(Context context) {
        mBroadcastManager = LocalBroadcastManager.getInstance(context);
    }

    /**
     * Add to the lap counter and publish the change.
     */
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
        mBroadcastManager.sendBroadcast(intent);
    }


    /**
     * Listen for the following events:
     *
     * STATE_TRANSITION -> if we get a state transition from FAR -> NEAR, count laps
     *
     * MISSED_LAPS -> If we reconnected and it is determined that the athlete went from
     *      FAR -> NEAR in the meantime, count laps
     */
    public void initCallbacks(SimpleMessageReceiver receiver) {
        receiver.registerHandler(LocationStateMachine.ACTION_STATE_TRANSITION, countLaps);
        receiver.registerHandler(DisconnectManager.ACTION_MISSED_LAPS, onMissedLaps);
    }
}
