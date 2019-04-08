package edu.drexel.lapcounter.lapcounter.backend.lapcounter;

import android.app.Service;
import android.arch.lifecycle.ViewModelProviders;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.IBinder;

import edu.drexel.lapcounter.lapcounter.backend.Database.Device.Device;
import edu.drexel.lapcounter.lapcounter.backend.Database.Device.DeviceDatabase;
import edu.drexel.lapcounter.lapcounter.backend.Database.Device.DeviceRepository;
import edu.drexel.lapcounter.lapcounter.backend.Database.Device.DeviceViewModel;
import edu.drexel.lapcounter.lapcounter.backend.SimpleMessageReceiver;
import edu.drexel.lapcounter.lapcounter.backend.ble.BLEService;

/**
 * This Service is the main interface to every part of the lap counting process. It owns
 * a few smaller components. These components are self-sufficient, however, the Service tells
 * them when is the appropriate time to start subscribing to Intents.
 */
public class LapCounterService extends Service {
    // For debugging
    private final static String TAG = LapCounterService.class.getSimpleName();

    // TODO: Load from calibration.
    private static final double DEFAULT_THRESHOLD = 60;
    /**
     * The location state machine tracks the state of the athlete.
     */
    private LocationStateMachine mStateMachine;

    /**
     * This component monitors the bluetooth service for disconnects/reconnects and
     * orchestrates changes to the state across components
     */
    private DisconnectManager mDisconnectManager;

    /**
     * This component simply counts laps (including publishing an event whenever a lap is counted.
     */
    private LapCounter mLapCounter;

    /**
     * This Service owns this simplified BroadcastReceiver so we only have one for the entire
     * service. The sub-components of the lap counting system will register callbacks with this
     * object as needed.
     */
    private SimpleMessageReceiver mReceiver;

    /**
     * a binder for this service
     */
    private final IBinder mBinder = new LocalBinder();

    /**
     * Binder implementation for LapCounterService
     */
    public class LocalBinder extends Binder {
        /**
         * Get a reference to this LapCounterService
         * @return this Service
         */
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

        reset();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
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
    }

    public void reset() {
        if (mReceiver != null)
            mReceiver.detach(this);

        mReceiver = new SimpleMessageReceiver();
        mStateMachine = new LocationStateMachine(this, DEFAULT_THRESHOLD);
        mDisconnectManager = new DisconnectManager(this);
        mLapCounter = new LapCounter(this);
        initCallbacks();
        mReceiver.attach(this);
    }

    public void updateThreshold(String mac) {
        DeviceRepository repo = new DeviceRepository(getApplication());
        Device device = repo.getDeviceByMacAddress(mac);
        mStateMachine.setThreshold(device.getThreshold());
    }
}
