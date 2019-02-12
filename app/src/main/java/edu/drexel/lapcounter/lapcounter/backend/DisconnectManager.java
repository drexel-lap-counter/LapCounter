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
     *
     */
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            switch(action) {
                case BLEService.ACTION_DEVICE_DISCONNECTED:
                    onDisconnect();
                    break;
                case BLEService.ACTION_DEVICE_CONNECTED:
                    onReconnect();
                    break;
                case LocationStateMachine.ACTION_STATE_TRANSITION:
                    onStateTransition();
                    break;
            }
        }
    };

    public DisconnectManager(Context context, RSSIManager rssi, LocationStateMachine stateMachine) {
        mContext = context;
        mRssiManager = rssi;
        mStateMachine = stateMachine;
    }

    void onDisconnect() {
        mReconnectFunc = new ReconnectFunction();
        ReconnectFunction.SwimmerState beforeSnapshot = mReconnectFunc.new SwimmerState();
        // TODO: Populate the snapshot with data queried from the rssiManager and state machine
        // also remember to add the timestamp

        mReconnectFunc.setBeforeState(beforeSnapshot);
    }

    void onReconnect() {
        // TODO: How to distinguish the first connection from reconnections?

        // TODO: Save the timestamp for later
        // TODO: yell at the RSSI component to poll faster

    }

    void onStateTransition() {
        // TODO: filter for only Unknown -> * events
        ReconnectFunction.SwimmerState afterSnapshot = mReconnectFunc.new SwimmerState();
        // TODO: Query the RSSI and state machine to populate the state
        boolean countLap = mReconnectFunc.computeLapsMissed(afterSnapshot);

        if (countLap)
            publishMissedLap();
    }

    /**
     * Publish a missed lap
     */
    void publishMissedLap() {
        Intent intent = new Intent(ACTION_MISSED_LAPS);
        mContext.sendBroadcast(intent);
    }
}
