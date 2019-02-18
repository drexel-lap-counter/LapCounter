package edu.drexel.lapcounter.lapcounter.backend.lapcounter;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.IBinder;

import edu.drexel.lapcounter.lapcounter.backend.ble.BLEComm;
import edu.drexel.lapcounter.lapcounter.backend.ble.BLEService;
import edu.drexel.lapcounter.lapcounter.backend.ble.RSSIManager;
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
    private DisconnectManager mDisconnectManager;
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

    private BLEService mBleService;

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
        bindToBleService();
    }

    private ServiceConnection mBleServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            BLEService.LocalBinder binder = (BLEService.LocalBinder) service;
            mBleService = binder.getService();

            mDisconnectManager = new DisconnectManager(LapCounterService.this, mBleService,
                                                       mStateMachine);

            initCallbacks();
            mReceiver.attach(LapCounterService.this);
        }

        public void onServiceDisconnected(ComponentName arg0) {
            mBleService = null;
        }
    };

    private void bindToBleService() {
        Intent intent = new Intent(this, BLEService.class);
        bindService(intent, mBleServiceConnection, BIND_AUTO_CREATE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mReceiver.detach(this);
        unbindService(mBleServiceConnection);
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
