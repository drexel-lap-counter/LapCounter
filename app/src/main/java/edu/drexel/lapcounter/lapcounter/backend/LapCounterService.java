package edu.drexel.lapcounter.lapcounter.backend;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class LapCounterService extends Service {

    /**
     * This component listens for raw RSSI values and applies filters to them to make
     * the data easier for other components to consume
     */
    private RSSIManager mRssiManager = new RSSIManager(this);

    /**
     * The location state machine tracks the state of the athlete.
     */
    private LocationStateMachine mStateMachine = new LocationStateMachine(this);

    /**
     * This component monitors the bluetooth service for disconnects/reconnects and
     * orchestrates changes to the state across components
     */
    private DisconnectManager mDisconnectManager =
            new DisconnectManager(this, mRssiManager, mStateMachine);

    /**
     * This component simply counts laps (including publishing an event whenever a lap is counted.
     */
    private LapCounter mLapCounter = new LapCounter(this);

    /**
     * This Service owns this simplified BroadcastReceiver so we only have one for the entire
     * service. The sub-components of the lap counting system will register callbacks with this
     * object as needed.
     */
    private SimpleMessageReceiver mReceiver = new SimpleMessageReceiver();

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        //TODO: Not sure if this is the best place for this code, review this before we finish
        initCallbacks();
        mReceiver.attach(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // TODO: Again, not sure if this is the best place for this or if we even need to
        // unbind the callbacks
        mReceiver.detatch(this);
    }

    /**
     * Go through the sub-components and have them all register callback functions for
     * whatever events are applicable.
     */
    private void initCallbacks() {
        mLapCounter.initCallbacks(mReceiver);
        mDisconnectManager.initCallbacks(mReceiver);
        mStateMachine.initCallbacks(mReceiver);
        mRssiManager.initCallbacks(mReceiver);
    }
}
