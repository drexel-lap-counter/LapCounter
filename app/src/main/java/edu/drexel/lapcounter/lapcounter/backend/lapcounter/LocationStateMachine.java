package edu.drexel.lapcounter.lapcounter.backend.lapcounter;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import edu.drexel.lapcounter.lapcounter.backend.SimpleMessageReceiver;
import edu.drexel.lapcounter.lapcounter.backend.ble.BLEComm;
import edu.drexel.lapcounter.lapcounter.backend.ble.IBroadcastManager;
import edu.drexel.lapcounter.lapcounter.backend.ble.RSSIManager;
import edu.drexel.lapcounter.lapcounter.backend.wrappers.LocalBroadcastManagerWrapper;

import static edu.drexel.lapcounter.lapcounter.backend.ble.RSSIManager.DIRECTION_IN;
import static edu.drexel.lapcounter.lapcounter.backend.ble.RSSIManager.DIRECTION_OUT;

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

    /**
     * This event is published whenever the state machine changes states.
     */
    public static final String ACTION_STATE_TRANSITION =
            "edu.drexel.lapcounter.lapcounter.ACTION_STATE_TRANSITION";

    /**
     * Each STATE_TRANSITION event includes a before state and an after state
     */
    public static final String EXTRA_STATE_BEFORE =
            "edu.drexel.lapcounter.lapcounter.EXTRA_STATE_BEFORE";

    /**
     * Each STATE_TRANSITION event includes a before state and an after state
     */
    public static final String EXTRA_STATE_AFTER =
            "edu.drexel.lapcounter.lapcounter.EXTRA_STATE_AFTER";

    /**
     * Utility object for broadcasting intents
     */
    private IBroadcastManager mBroadcastManager;

    /**
     * Current state
     */
    private State mState = State.UNKNOWN;

    /**
     * The current RSSI threshold between NEAR and FAR
     */
    private double mThreshold;

    /**
     * Constructor
     * @param context the parent LapCounterService
     * @param threshold Threshold to use from the device callibration settings
     */
    public LocationStateMachine(Context context, double threshold) {
        this(LocalBroadcastManagerWrapper.getInstance(context), threshold);
    }

    /**
     * Constructor for unit testing.
     * @param broadcastManager the wrapper around a LocalBroadcastManager
     * @param threshold the RSSI threshold
     */
    public LocationStateMachine(IBroadcastManager broadcastManager, double threshold) {
        mBroadcastManager = broadcastManager;
        mThreshold = threshold;
    }

    /**
     * Change the threshold value
     * @param threshold the new RSSI threshold
     */
    public void setThreshold(double threshold) {
        this.mThreshold = threshold;
    }

    /**
     * Check for the Near -> Far transition
     * @param rssi the current RSSI value
     * @param direction the current direction of travel (sign of the RSSI delta)
     * @return true if this is a Near -> Far transition
     */
    private boolean crossedAndMovingAwayFromThreshold(double rssi, int direction) {
        return mState == State.NEAR &&     // We were previously within the threshold,
               rssi > mThreshold    &&     // and then we crossed it,
               direction == DIRECTION_OUT; // by moving away from it.
    }

    /**
     * Check for the Far -> Near transition
     * @param rssi the current RSSI value
     * @param direction the current direction of travel (sign of the RSSI delta)
     * @return true if this is a Far -> Near transition
     */
    private boolean crossedAndWithinThreshold(double rssi, int direction) {
        return mState == State.FAR &&
               rssi <= mThreshold  &&
               direction == DIRECTION_IN;
    }

    // Intent handling callbacks ===============================================================

    /**
     * Whenever we get a filtered RSSI value, update the state
     */
    private SimpleMessageReceiver.MessageHandler onRssiAndDirection = new SimpleMessageReceiver.MessageHandler() {
        @Override
        public void onMessage(Intent message) {
            double rssi = message.getDoubleExtra(RSSIManager.EXTRA_RSSI, 0);
            int direction = message.getIntExtra(RSSIManager.EXTRA_DIRECTION, 0);

            if (mState == State.UNKNOWN) {
                // On the first valid RSSI when we're in an unknown state, determine whether
                // we are now in the NEAR or FAR zone
                pickZone(rssi);
            } else if (crossedAndMovingAwayFromThreshold(rssi, direction)) {
                // Publish a Near -> Far transition
                mState = State.FAR;
                publishStateTransition(State.NEAR, mState);
            } else if (crossedAndWithinThreshold(rssi, direction)) {
                // Publish a Far -> Near transition
                mState = State.NEAR;
                publishStateTransition(State.FAR, mState);
            }
        }
    };

    /**
     * if a device disconnects, go back to the UNKNOWN state
     */
    private SimpleMessageReceiver.MessageHandler onDisconnect = new SimpleMessageReceiver.MessageHandler() {
        @Override
        public void onMessage(Intent message) {
            State oldState = mState;
            mState = State.UNKNOWN;

            publishStateTransition(oldState, mState);
        }
    };

    // =========================================================================================

    /**
     * Publish an Intent giving information about what transition was made
     * @param before state before the transition
     * @param after state after the transition
     */
    private void publishStateTransition(State before, State after) {
        Intent intent = new Intent(ACTION_STATE_TRANSITION);
        intent.putExtra(EXTRA_STATE_BEFORE, before);
        intent.putExtra(EXTRA_STATE_AFTER, after);
        mBroadcastManager.sendBroadcast(intent);
    }

    /**
     * Subscribe to the following events:
     *
     * RSSI_AVAILABLE -> update the current state
     *
     * DISCONNECTED -> switch to the UNKNOWN State
     * @param receiver helper object for subscribing to events
     */
    public void initCallbacks(SimpleMessageReceiver receiver) {
        receiver.registerHandler(RSSIManager.ACTION_RSSI_AND_DIR_AVAILABLE, onRssiAndDirection);
        receiver.registerHandler(BLEComm.ACTION_DISCONNECTED, onDisconnect);
    }

    /**
     * As soon as we get a first valid filtered RSSI value while still in the UNKNOWN state,
     * compare the RSSI value with the threshold and transition to the corresponding state
     * @param rssi the filtered RSSI value (in absolute value)
     */
    public void pickZone(double rssi) {
        if (rssi > mThreshold)
            mState = State.FAR;
        else
            mState = State.NEAR;

        publishStateTransition(State.UNKNOWN, mState);
    }
}
