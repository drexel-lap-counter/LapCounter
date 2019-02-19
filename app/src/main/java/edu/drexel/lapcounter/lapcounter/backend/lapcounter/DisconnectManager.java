package edu.drexel.lapcounter.lapcounter.backend.lapcounter;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import edu.drexel.lapcounter.lapcounter.backend.BLEService;
import edu.drexel.lapcounter.lapcounter.backend.RSSIManager;
import edu.drexel.lapcounter.lapcounter.backend.SimpleMessageReceiver;

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

    private SimpleMessageReceiver.MessageHandler onReconnect = new SimpleMessageReceiver.MessageHandler() {
        @Override
        public void onMessage(Intent message) {
            // We want the timestamp of reconnection, even though we are not done with the
            // logic for a second or two while the RSSIManager refills the buffer. So save it
            // in a member variable.
            mCurrentState.timestamp = System.currentTimeMillis();
        }
    };

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

    private SimpleMessageReceiver.MessageHandler onRssi = new SimpleMessageReceiver.MessageHandler() {
        @Override
        public void onMessage(Intent message) {
            // Store the current RSSI value in the state snapshot
            mCurrentState.distRssi = message.getDoubleExtra(RSSIManager.EXTRA_RSSI, 0.0);
            mCurrentState.travelDirection = message.getIntExtra(RSSIManager.EXTRA_DIRECTION, 0);
        }
    };

    // =========================================================================================

    public DisconnectManager(Context context) {
        mBroadcastManager = LocalBroadcastManager.getInstance(context);
    }


    /**
     * Handle the following intents:
     *
     * DEVICE_DISCONNECTED -> Alert other components of the disconnect while keeping track
     *      of the current state to help with the reconnect logic
     *
     * DEVICE_CONNECTED -> start the reconnection process
     *
     * STATE_TRANSITION -> Listen for transitions out of the unknown state. If this is because
     *      of a reconnection,
     */
    void initCallbacks(SimpleMessageReceiver receiver) {
        receiver.registerHandler(RSSIManager.ACTION_RSSI_AND_DIR_AVAILABLE, onRssi);
        receiver.registerHandler(BLEService.ACTION_DEVICE_DISCONNECTED, onDisconnect);
        //TODO: This should be reconnect events only.
        receiver.registerHandler(BLEService.ACTION_DEVICE_CONNECTED, onReconnect);
        receiver.registerHandler(LocationStateMachine.ACTION_STATE_TRANSITION, onTransition);
    }
    /**
     * Publish a missed lap
     */
    private void publishMissedLap() {
        Intent intent = new Intent(ACTION_MISSED_LAPS);
        mBroadcastManager.sendBroadcast(intent);
    }
}
