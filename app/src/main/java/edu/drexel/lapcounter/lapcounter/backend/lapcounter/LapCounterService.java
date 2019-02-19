package edu.drexel.lapcounter.lapcounter.backend.lapcounter;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import edu.drexel.lapcounter.lapcounter.backend.RSSIManager;
import edu.drexel.lapcounter.lapcounter.backend.SimpleMessageReceiver;

public class LapCounterService extends Service {
    private final static String TAG = LapCounterService.class.getSimpleName();

    /**
     * This component listens for raw RSSI values and applies filters to them to make
     * the data easier for other components to consume
     */
    private RSSIManager mRssiManager = new RSSIManager(this);

    // TODO: Load from calibration.
    private static final double DEFAULT_THRESHOLD = 60;
    /**
     * The location state machine tracks the state of the athlete.
     */
    private LocationStateMachine mStateMachine = new LocationStateMachine(this, DEFAULT_THRESHOLD);

    /**
     * This component monitors the bluetooth service for disconnects/reconnects and
     * orchestrates changes to the state across components
     */
    private DisconnectManager mDisconnectManager = new DisconnectManager(this);

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

/*
NOTE: https://developer.android.com/guide/components/bound-services

Questions:
    1. Does a bound service continue to run when all bound clients are suspended, i.e., in the
    back stack?

    2. If no, then can another Activity _not_ bound to the service still receive
    broadcasts from the service?

Something to keep in mind for future Royce:
"Although you usually implement either onBind() or onStartCommand(), it's sometimes necessary to
implement both. For example, a music player might find it useful to allow its service to run
indefinitely and also provide binding. This way, an activity can start the service to play some
music and the music continues to play even if the user leaves the application. Then, when the
user returns to the application, the activity can bind to the service to regain control of
playback."
*/

    private final IBinder mBinder = new LocalBinder();

    public class LocalBinder extends Binder {
        public LapCounterService getService() {
            return LapCounterService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
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
        mReceiver.detach(this);
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
