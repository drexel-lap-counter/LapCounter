package edu.drexel.lapcounter.lapcounter.backend.lapcounter;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import edu.drexel.lapcounter.lapcounter.backend.SimpleMessageReceiver;
import edu.drexel.lapcounter.lapcounter.backend.ble.BLEComm;
import edu.drexel.lapcounter.lapcounter.backend.ble.RSSIManager;

/**
 * This class listens for BLE disconnect/reconnect events and using information about the
 * RSSI and state, determine if the athlete missed any laps
 */
public class DisconnectManager {
    /**
     * This intent is published on a reconnection event when the ReconnectFunction determines
     * that an extra lap should be counted
     */
    public static final String ACTION_MISSED_LAPS =
            "edu.drexel.lapcounter.lapcounter.ACTION_MISSED_LAPS";

    /**
     * Used to publish events.
     */
    private LocalBroadcastManager mBroadcastManager;

    /**
     * When the bluetooth device reconnects to the phone, it's rather complicated
     * to handle what happens. We decided to wrap the logic up in a dedicated class.
     */
    private ReconnectFunction mReconnectFunc = null;

    /**
     * Current state of the athlete. This is updated on receiving incoming intents. As needed,
     * snapshots of this state are passed to the reconnect function
     */
    private AthleteState mCurrentState = new AthleteState();

    // Callbacks =================================================================================
    /**
     * On disconnect, take a "before disconnect" snapshot of the current state and start setting up
     * the reconnect function. We need to wait for after reconnection to get the "after" state.
     */
    private SimpleMessageReceiver.MessageHandler onDisconnect = new SimpleMessageReceiver.MessageHandler() {
        @Override
        public void onMessage(Intent message) {
            mReconnectFunc = new ReconnectFunction();

            // Save the current time
            mCurrentState.timestamp = System.currentTimeMillis();

            // Add the before state to the reconnection function.
            mReconnectFunc.setBeforeState(mCurrentState.copy());
        }
    };

    /**
     * As soon as we get a reconnection event, save a timestamp of reconnection, but we have
     * to wait a little bit for the filtered RSSI values to start trickling through.
     */
    private SimpleMessageReceiver.MessageHandler onReconnect = new SimpleMessageReceiver.MessageHandler() {
        @Override
        public void onMessage(Intent message) {
            boolean isReconnect = message.getBooleanExtra(BLEComm.EXTRA_IS_RECONNECT, false);

            if (!isReconnect) {
                return;
            }

            // We want the timestamp of reconnection, even though we are not done with the
            // logic for a second or two while the RSSIManager refills the buffer. So save it
            // in a member variable.
            mCurrentState.timestamp = System.currentTimeMillis();
        }
    };

    /**
     * When we hear of a transition from a state machine, update the current state (only if the
     * new state is near/far).
     *
     * If the transition is from unknown -> near/far, we now have enough information to
     * compute the "after reconnect" snapshot. Now we can compute any missed laps and publish
     * them if needed.
     */
    private SimpleMessageReceiver.MessageHandler onTransition = new SimpleMessageReceiver.MessageHandler() {
        @Override
        public void onMessage(Intent message) {
            LocationStateMachine.State beforeState = (LocationStateMachine.State)
                    message.getSerializableExtra(LocationStateMachine.EXTRA_STATE_BEFORE);
            LocationStateMachine.State afterState = (LocationStateMachine.State)
                    message.getSerializableExtra(LocationStateMachine.EXTRA_STATE_AFTER);

            // Update the current zone, we'll need this for the reconnection function.
            if (afterState != LocationStateMachine.State.UNKNOWN) {
                mCurrentState.zone = afterState;
            }

            // We only care about Unknown -> any other state. Filter out irrelevant transitions here
            if (beforeState != LocationStateMachine.State.UNKNOWN)
                return;

            // Use the reconnection function to examine the state and determine if we
            // should count a missed lap.
            boolean countLap = mReconnectFunc.computeLapsMissed(mCurrentState.copy());
            if (countLap)
                publishMissedLap();
        }
    };

    /**
     * When we get a filtered RSSI value message, save the RSSI and direction.
     * These values are only used to create the "before disconnect" or "after reconnect"
     * state snapshots as needed.
     */
    private SimpleMessageReceiver.MessageHandler onRssi = new SimpleMessageReceiver.MessageHandler() {
        @Override
        public void onMessage(Intent message) {
            // Store the current RSSI value in the state snapshot
            mCurrentState.distRssi = message.getDoubleExtra(RSSIManager.EXTRA_RSSI, 0.0);
            mCurrentState.travelDirection = message.getIntExtra(RSSIManager.EXTRA_DIRECTION, 0);
        }
    };

    // =========================================================================================

    /**
     * Constructor
     * @param context the parent service for setting up Intent broadcasts
     */
    public DisconnectManager(Context context) {
        mBroadcastManager = LocalBroadcastManager.getInstance(context);
    }


    /**
     * Handle the following intents:
     *
     * DISCONNECTED -> Take a snapshot of the athlete's state just before the disconnection
     *
     * RECONNECTED -> Start the reconnection process
     *
     * STATE_TRANSITION -> If the transition is to near/far, just keep track of this.
     *      If the transition is from unknown -> near/far, also compute the ReconnectFunction
     *      to see if we missed any laps
     *
     * RSSI_AVAILABLE -> Store the RSSI for use in computing before/after snapshots
     */
    void initCallbacks(SimpleMessageReceiver receiver) {
        receiver.registerHandler(BLEComm.ACTION_DISCONNECTED, onDisconnect);
        receiver.registerHandler(BLEComm.ACTION_CONNECTED, onReconnect);
        receiver.registerHandler(LocationStateMachine.ACTION_STATE_TRANSITION, onTransition);
        receiver.registerHandler(RSSIManager.ACTION_RSSI_AND_DIR_AVAILABLE, onRssi);
    }
    /**
     * Publish a missed lap
     */
    private void publishMissedLap() {
        Intent intent = new Intent(ACTION_MISSED_LAPS);
        mBroadcastManager.sendBroadcast(intent);
    }
}
