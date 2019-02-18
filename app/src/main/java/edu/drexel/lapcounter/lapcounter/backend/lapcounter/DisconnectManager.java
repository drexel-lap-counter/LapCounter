package edu.drexel.lapcounter.lapcounter.backend.lapcounter;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import edu.drexel.lapcounter.lapcounter.backend.ble.BLEComm;
import edu.drexel.lapcounter.lapcounter.backend.ble.BLEService;
import edu.drexel.lapcounter.lapcounter.backend.ble.RSSIManager;
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

    // References to other components for direct querying
    private BLEService mBleService;
    private LocationStateMachine mStateMachine;

    // Keep track of the time of reconnection.
    private long mAfterTimestamp = -1;

    // Callbacks =================================================================================
    private SimpleMessageReceiver.MessageHandler onDisconnect = new SimpleMessageReceiver.MessageHandler() {
        @Override
        public void onMessage(Intent message) {
            mReconnectFunc = new ReconnectFunction();
            ReconnectFunction.AthleteState beforeSnapshot = mReconnectFunc.new AthleteState();

            // Save what zone the athlete is in. Then set the state to UNKNOWN
            beforeSnapshot.zone = mStateMachine.getZone();
            mStateMachine.onDisconnect();

            beforeSnapshot.distRssi = mBleService.getRssi();
            beforeSnapshot.travelDirection = mBleService.getDirection();
            mBleService.clearRssiManager();

            // Save the current time
            beforeSnapshot.timestamp = System.currentTimeMillis();

            // Add the before state to the reconnection function.
            mReconnectFunc.setBeforeState(beforeSnapshot);
        }
    };

    private SimpleMessageReceiver.MessageHandler onReconnect = new SimpleMessageReceiver.MessageHandler() {
        @Override
        public void onMessage(Intent message) {
            // We want the timestamp of reconnection, even though we are not done with the
            // logic for a second or two while the RSSIManager refills the buffer. So save it
            // in a member variable.
            mAfterTimestamp = System.currentTimeMillis();
        }
    };

    private SimpleMessageReceiver.MessageHandler onTransition = new SimpleMessageReceiver.MessageHandler() {
        @Override
        public void onMessage(Intent message) {
            LocationStateMachine.State beforeState = (LocationStateMachine.State)
                    message.getSerializableExtra(LocationStateMachine.EXTRA_STATE_BEFORE);
            LocationStateMachine.State afterState = (LocationStateMachine.State)
                    message.getSerializableExtra(LocationStateMachine.EXTRA_STATE_AFTER);

            // We only care about Unknown -> any other state. Filter out irrelevant transitions here
            if (beforeState != LocationStateMachine.State.UNKNOWN)
                return;

            // Now we have enough information to complete an after reconnection state snapshot.
            ReconnectFunction.AthleteState afterSnapshot = mReconnectFunc.new AthleteState();
            afterSnapshot.zone = afterState;

            // Store the timestamp and clear the internal state.
            afterSnapshot.timestamp = mAfterTimestamp;
            mAfterTimestamp = -1;

            // Get the athlete's new position and direction.
            afterSnapshot.distRssi = mBleService.getRssi();
            afterSnapshot.travelDirection = mBleService.getDirection();

            // Use the reconnection function to examine the state and determine if we
            // should count a missed lap.
            boolean countLap = mReconnectFunc.computeLapsMissed(afterSnapshot);
            if (countLap)
                publishMissedLap();
        }
    };

    // =========================================================================================

    public DisconnectManager(Context context, BLEService bleService, LocationStateMachine stateMachine) {
        mBroadcastManager = LocalBroadcastManager.getInstance(context);
        mBleService = bleService;
        mStateMachine = stateMachine;
    }


    /**
     * Handle the following intents:
     *
     * DEVICE_DISCONNECTED -> Alert other components of the disconnect while keeping track
     *      of the current state to help with the reconnect logic
     *
     * DEVICE_RECONNECTED -> Start the reconnection process
     *
     * STATE_TRANSITION -> Listen for transitions out of the unknown state. If this is because
     *      of a reconnection,
     */
    void initCallbacks(SimpleMessageReceiver receiver) {
        receiver.registerHandler(BLEComm.ACTION_DISCONNECTED, onDisconnect);
        receiver.registerHandler(BLEComm.ACTION_RECONNECTED, onReconnect);
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
