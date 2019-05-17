package edu.drexel.lapcounter.lapcounter.backend.lapcounter;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import edu.drexel.lapcounter.lapcounter.backend.Database.Device.Device;
import edu.drexel.lapcounter.lapcounter.backend.Database.Device.DeviceRepository;
import edu.drexel.lapcounter.lapcounter.backend.SimpleMessageReceiver;

/**
 * This Service is the main interface to every part of the lap counting process. It owns
 * a few smaller components. These components are self-sufficient, however, the Service tells
 * them when is the appropriate time to start subscribing to Intents.
 */
public class LapCounterService extends Service {
    /** logging tag */
    private final static String TAG = LapCounterService.class.getSimpleName();

    /**
     * Default threshold before loading from SharedPreferences.
     */
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
     * This service logs transitions in the current workout, and upon the save
     * button being pressed, it flushes these transitions to the database.
     */
    private TransitionLog mLog;

    /**
     * a binder for this service
     */
    private final IBinder mBinder = new LocalBinder();

    /**
     * Constructor not used because services are launched by binding.
     */
    @SuppressWarnings("unused")
    public LapCounterService() {}

    /**
     * Constructor for mocking in unit tests
     * @param lsm the location state machine component
     * @param dm the disconnect manager component
     * @param lc the lap counter component
     * @param r the message receiver component
     */
    public LapCounterService(LocationStateMachine lsm, DisconnectManager dm, LapCounter lc,
                             SimpleMessageReceiver r) {
        mStateMachine = lsm;
        mDisconnectManager = dm;
        mLapCounter = lc;
        mReceiver = r;
    }

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
    public void initCallbacks() {
        mLapCounter.initCallbacks(mReceiver);
        mDisconnectManager.initCallbacks(mReceiver);
        mStateMachine.initCallbacks(mReceiver);
        mLog.initCallbacks(mReceiver);
    }

    /**
     * Reset the lap counter by unsubscribing from all events and re-instantiating the
     * sub-components.
     */
    public void reset() {
        if (mReceiver != null)
            mReceiver.detach(this);

        mReceiver = new SimpleMessageReceiver();
        mStateMachine = new LocationStateMachine(this, DEFAULT_THRESHOLD);
        mDisconnectManager = new DisconnectManager(this);
        mLapCounter = new LapCounter(this);
        mLog = new TransitionLog(this);
        initCallbacks();
        mReceiver.attach(this);
    }

    /**
     * Update the RSSI threshold to match that of the currently connected device.
     * @param mac the MAC address of the device to look up.
     */
    public void updateThreshold(String mac) {
        DeviceRepository repo = new DeviceRepository(getApplication());
        Device device = repo.getDeviceByMacAddress(mac);
        mStateMachine.setThreshold(device.getThreshold());
    }
}
