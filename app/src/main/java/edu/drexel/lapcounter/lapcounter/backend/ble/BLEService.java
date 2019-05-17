package edu.drexel.lapcounter.lapcounter.backend.ble;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import edu.drexel.lapcounter.lapcounter.backend.SimpleMessageReceiver;

/**
 * The BLE Service and its sub-components handle low-level Bluetooth Low Energy details.
 * This includes connecting to Bluetooth devices and processing RSSI values
 */
public class BLEService extends Service {
    /**
     * The BLEComm component handles low-level details of connecting to the Bluetooth device
     */
    private BLEComm mBleComm;
    /**
     * The RSSIManager listens for raw RSSI values and filters them into formats needed by other
     * components of the system.
     */
    private RSSIManager mRssiManager;

    /**
     * Binder for this Service
     */
    private final IBinder mBinder = new LocalBinder();

    /**
     * Message receiver for subscribing to events.
     */
    private SimpleMessageReceiver mReceiver;

    @SuppressWarnings("unused")
    BLEService() {}

    /**
     * Constructor used for unit tests
     * @param comm the BLE Comm component
     * @param rssiManager The RSSI Manager component
     * @param receiver The message receiver utility
     */
    public BLEService(BLEComm comm, RSSIManager rssiManager, SimpleMessageReceiver receiver) {
        mBleComm = comm;
        mRssiManager = rssiManager;
        mReceiver = receiver;
    }

    /**
     * The LocalBinder pattern, common for Android Services.
     */
    public class LocalBinder extends Binder {
        public BLEService getService() {
            return BLEService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mBleComm = new BLEComm(this);
        reset();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        disconnectDevice();
        mReceiver.detach(this);
    }

    /**
     * Start scanning for new devices
     * @param scanCallback the callback called once for every device found
     */
    public void startScan(BluetoothAdapter.LeScanCallback scanCallback) {
        mBleComm.startScan(scanCallback);
    }

    /**
     * Stop scanning for new devices
     * @param scanCallback the callback called once for each new device.
     */
    public void stopScan(BluetoothAdapter.LeScanCallback scanCallback) {
        mBleComm.stopScan(scanCallback);
    }

    /**
     * Connect to a specific bluetooth deviice
     * @param deviceAddress the MAC of the device to connect to
     */
    public void connectToDevice(String deviceAddress) {
        mBleComm.connect(deviceAddress);
    }

    /**
     * Disconnect a Bluetooth device
     */
    public void disconnectDevice() {
        mBleComm.disconnect();
    }

    /**
     * Start listening for raw RSSI values
     */
    public void startRssiRequests() {
        mRssiManager.scheduleRssiRequest();
    }

    /**
     * Stop listening for raw RSSI values
     */
    public void stopRssiRequests() {
        mRssiManager.stopRssiRequests();
    }

    /**
     * Set the window sizes for the RSSIManager's sliding windows
     * @param deltasSize the size of the window for the delta RSSI buffer
     * @param filterSize the size of the window for the RSSI low-pass filter
     */
    public void setRssiManagerWindowSizes(int deltasSize, int filterSize) {
        mRssiManager.setDeltasSize(deltasSize);
        mRssiManager.setFilterSize(filterSize);
    }

    /**
     * When we need to reset the service, do the following::
     * - Unsubscribe from events
     * - Reset the BLEComm component to close Bluetooth connections
     * - discard old components and replace with new versions.
     */
    public void reset() {
        // Avoid potential race conditions before garbage collection
        if (mReceiver != null) {
            mReceiver.detach(this);
        }

        mBleComm.reset();
        mReceiver = new SimpleMessageReceiver();
        mRssiManager = new RSSIManager(this, mBleComm);
        mRssiManager.initCallbacks(mReceiver);
        mReceiver.attach(this);
    }
}