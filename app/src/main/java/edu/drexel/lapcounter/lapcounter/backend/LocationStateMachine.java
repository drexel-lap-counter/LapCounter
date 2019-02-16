package edu.drexel.lapcounter.lapcounter.backend;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import static edu.drexel.lapcounter.lapcounter.backend.RSSIManager.DIRECTION_IN;
import static edu.drexel.lapcounter.lapcounter.backend.RSSIManager.DIRECTION_OUT;

/**
 * This class keeps track of whether the athlete is "near" or "far" from the phone. It publishes
 * state changes as intents.
 */
public class LocationStateMachine {

    /**
     * States of the machine
     */
    public enum State {
        /**
         * Unknown indicates that we do not currently have a valid estimate of the
         * athlete's position. This happens for two reasons:
         *
         * 1. At the start of lap counting, the buffers for filtering RSSI values are not yet
         *      full, so the averages are not yet valid.
         * 2. On a disconnect, we cannot be sure of where the athlete is
         */
        UNKNOWN,
        /**
         * This represents that the athlete is currently near the phone (within the threshold)
         */
        NEAR,
        /**
         * This represents that the athlete is far away from the phone (past the threshold)
         */
        FAR,
    }

    public static final String ACTION_STATE_TRANSITION =
            "edu.drexel.lapcounter.lapcounter.ACTION_STATE_TRANSITION";

    public static final String EXTRA_STATE_BEFORE =
            "edu.drexel.lapcounter.lapcounter.EXTRA_STATE_BEFORE";

    public static final String EXTRA_STATE_AFTER =
            "edu.drexel.lapcounter.lapcounter.EXTRA_STATE_AFTER";

    // Used to publish events
    private LocalBroadcastManager mBroadcastManager;

    // Current state
    private State mState;

    // RSSI threshold that separates State.NEAR and State.FAR.
    private double mThreshold;

    public LocationStateMachine(Context context, double threshold) {
        mBroadcastManager = LocalBroadcastManager.getInstance(context);
        mThreshold = threshold;
    }

    private boolean crossedAndMovingAwayFromThreshold(double rssi, int direction) {
        return mState == State.NEAR &&     // We were previously within the threshold,
               rssi > mThreshold    &&     // and then we crossed it,
               direction == DIRECTION_OUT; // by moving away from it.
    }

    private boolean crossedAndWithinThreshold(double rssi, int direction) {
        return mState == State.FAR &&
               rssi <= mThreshold  &&
               direction == DIRECTION_IN;
    }

    private SimpleMessageReceiver.MessageHandler onRssiAndDirection = new SimpleMessageReceiver.MessageHandler() {
        @Override
        public void onMessage(Intent message) {
            double rssi = message.getDoubleExtra(RSSIManager.EXTRA_RSSI, 0);
            int direction = message.getIntExtra(RSSIManager.EXTRA_DIRECTION, 0);

            if (crossedAndMovingAwayFromThreshold(rssi, direction)) {
                mState = State.FAR;
                publishStateTransition(State.NEAR, mState);
            } else if (crossedAndWithinThreshold(rssi, direction)) {
                mState = State.NEAR;
                publishStateTransition(State.FAR, mState);
            }
        }
    };

    private void publishStateTransition(State before, State after) {
        Intent intent = new Intent(ACTION_STATE_TRANSITION);
        intent.putExtra(EXTRA_STATE_BEFORE, before);
        intent.putExtra(EXTRA_STATE_AFTER, after);
        mBroadcastManager.sendBroadcast(intent);
    }

    public void initCallbacks(SimpleMessageReceiver receiver) {
        receiver.registerHandler(RSSIManager.ACTION_RSSI_AND_DIR_AVAILABLE, onRssiAndDirection);
    }

    public State getZone() {
        return mState;
    }

    public void pickZone() {
        // TODO: Unknown -> Near/Far
    }

    public void reset() {
        mState = State.UNKNOWN;
    }
}
