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

    // How often the RSSIManager should poll for the RSSI after connecting.
    private static final int CONNECT_RSSI_POLL_MS = 100;

    // References to other components for direct querying
    private RSSIManager mRssiManager;
    private LocationStateMachine mStateMachine;

    // Callbacks =================================================================================
    private SimpleMessageReceiver.MessageHandler onDisconnect = new SimpleMessageReceiver.MessageHandler() {
        @Override
        public void onMessage(Intent message) {
            mReconnectFunc = new ReconnectFunction();
            ReconnectFunction.AthleteState beforeSnapshot = mReconnectFunc.new AthleteState();

            beforeSnapshot.zone = mStateMachine.getZone();
            beforeSnapshot.distRssi = mRssiManager.getRssi();
            beforeSnapshot.travelDirection = mRssiManager.getDirection();
            beforeSnapshot.timestamp = System.currentTimeMillis();

            mReconnectFunc.setBeforeState(beforeSnapshot);

            mRssiManager.clear();
            mStateMachine.reset();
        }
    };

    private SimpleMessageReceiver.MessageHandler onConnect = new SimpleMessageReceiver.MessageHandler() {
        @Override
        public void onMessage(Intent message) {
            // TODO: How to distinguish the first connection from reconnections?
            // On connect, save the device address.
            // On connect, check the device address. If the device address is the same, then
            // we'll consider this connect to be a reconnect.



            // TODO: Save the timestamp for later
            // TODO: yell at the RSSI component to poll faster
            mRssiManager.setPollFrequency(CONNECT_RSSI_POLL_MS);
        }
    };

    private SimpleMessageReceiver.MessageHandler onTransition = new SimpleMessageReceiver.MessageHandler() {
        @Override
        public void onMessage(Intent message) {
            // TODO: filter for only Unknown -> * events
            ReconnectFunction.AthleteState afterSnapshot = mReconnectFunc.new AthleteState();
            // TODO: Query the RSSI and state machine to populate the state
            boolean countLap = mReconnectFunc.computeLapsMissed(afterSnapshot);

            if (countLap)
                publishMissedLap();
        }
    };

    // =========================================================================================

    public DisconnectManager(Context context, RSSIManager rssi, LocationStateMachine stateMachine) {
        mBroadcastManager = LocalBroadcastManager.getInstance(context);
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
        receiver.registerHandler(BLEService.ACTION_DEVICE_CONNECTED, onConnect);
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
