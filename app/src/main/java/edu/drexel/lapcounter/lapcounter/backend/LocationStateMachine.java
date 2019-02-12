package edu.drexel.lapcounter.lapcounter.backend;

import android.content.Context;

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
        FAR
    }

    public static final String ACTION_STATE_TRANSITION =
            "edu.drexel.lapcounter.lapcounter.ACTION_STATE_TRANSITION";

    // A context for publishing events
    private Context mContext;

    // Current state
    private State mState;

    public LocationStateMachine(Context context) {
        mContext = context;
    }
}
