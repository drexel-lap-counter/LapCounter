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
     * This service logs transitions in the current workout, and upon the save
     * button being pressed, it flushes these transitions to the database.
     */
    private TransitionLog mLog;

    /**
     * a binder for this service
     */
    private final IBinder mBinder = new LocalBinder();

    @SuppressWarnings("unused")
    public LapCounterService() {}

    // Constructor for mocking in unit tests.
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

    public void updateThreshold(String mac) {
        DeviceRepository repo = new DeviceRepository(getApplication());
        Device device = repo.getDeviceByMacAddress(mac);
        mStateMachine.setThreshold(device.getThreshold());
    }
}
