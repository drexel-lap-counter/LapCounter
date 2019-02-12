package edu.drexel.lapcounter.lapcounter.backend;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class DisconnectManager {
    /**
     * This intent is published on a reconnection event when the ReeconnectFunction determines
     * that an extra lap should be counted
     */
    public static final String ACTION_MISSED_LAPS =
            "edu.drexel.lapcounter.lapcounter.ACTION_MISSED_LAPS";

    /**
     * parent service for publishing intents.
     */
    private Context mContext;

    /**
     * When the bluetooth device reconnects to the phone, it's rather complicated
     * to handle what happens. We decided to wrap the logic up in a dedicated class.
     */
    private ReconnectFunction mReconnectFunc = null;

    // References to other components for direct querying
    private RSSIManager mRssiManager;
    private LocationStateMachine mStateMachine;


    // Callbacks =================================================================================
    private SimpleMessageReceiver.MessageHandler onDisconnect = new SimpleMessageReceiver.MessageHandler() {
        @Override
        public void onMessage(Intent message) {
            mReconnectFunc = new ReconnectFunction();
            ReconnectFunction.SwimmerState beforeSnapshot = mReconnectFunc.new SwimmerState();
            // TODO: Populate the snapshot with data queried from the rssiManager and state machine
            // also remember to add the timestamp

            mReconnectFunc.setBeforeState(beforeSnapshot);
        }
    };

    private SimpleMessageReceiver.MessageHandler onReconnect = new SimpleMessageReceiver.MessageHandler() {
        @Override
        public void onMessage(Intent message) {
            // TODO: How to distinguish the first connection from reconnections?

            // TODO: Save the timestamp for later
            // TODO: yell at the RSSI component to poll faster
        }
    };

    private SimpleMessageReceiver.MessageHandler onTransition = new SimpleMessageReceiver.MessageHandler() {
        @Override
        public void onMessage(Intent message) {
            // TODO: filter for only Unknown -> * events
            ReconnectFunction.SwimmerState afterSnapshot = mReconnectFunc.new SwimmerState();
            // TODO: Query the RSSI and state machine to populate the state
            boolean countLap = mReconnectFunc.computeLapsMissed(afterSnapshot);

            if (countLap)
                publishMissedLap();
        }
    };

    // =========================================================================================

    public DisconnectManager(Context context, RSSIManager rssi, LocationStateMachine stateMachine) {
        mContext = context;
        mRssiManager = rssi;
        mStateMachine = stateMachine;
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
        receiver.registerHandler(BLEService.ACTION_DEVICE_DISCONNECTED, onDisconnect);
        receiver.registerHandler(BLEService.ACTION_DEVICE_CONNECTED, onReconnect);
        receiver.registerHandler(LocationStateMachine.ACTION_STATE_TRANSITION, onTransition);
    }
    /**
     * Publish a missed lap
     */
    void publishMissedLap() {
        Intent intent = new Intent(ACTION_MISSED_LAPS);
        mContext.sendBroadcast(intent);
    }
}
