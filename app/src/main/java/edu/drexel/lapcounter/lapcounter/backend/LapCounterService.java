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

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    // TODO: not sure where to trigger registering the broadcastReceivers.
    // perhaps in onCreeate() and onDestroy()?
}
