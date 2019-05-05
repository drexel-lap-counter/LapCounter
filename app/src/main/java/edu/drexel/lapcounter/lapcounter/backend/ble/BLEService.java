package edu.drexel.lapcounter.lapcounter.backend.ble;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import edu.drexel.lapcounter.lapcounter.backend.SimpleMessageReceiver;

public class BLEService extends Service {
    private BLEComm mBleComm;
    private RSSIManager mRssiManager;

    private final IBinder mBinder = new LocalBinder();

    private SimpleMessageReceiver mReceiver;

    public BLEService(BLEComm comm, RSSIManager rssiManager, SimpleMessageReceiver receiver) {
        mBleComm = comm;
        mRssiManager = rssiManager;
        mReceiver = receiver;
    }

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

    public void startScan(BluetoothAdapter.LeScanCallback scanCallback) {
        mBleComm.startScan(scanCallback);
    }

    public void stopScan(BluetoothAdapter.LeScanCallback scanCallback) {
        mBleComm.stopScan(scanCallback);
    }

    public void connectToDevice(String deviceAddress) {
        mBleComm.connect(deviceAddress);
    }

    public void disconnectDevice() {
        mBleComm.disconnect();
    }

    public void startRssiRequests() {
        mRssiManager.scheduleRssiRequest();
    }

    public void stopRssiRequests() {
        mRssiManager.stopRssiRequests();
    }

    public void setRssiManagerWindowSizes(int deltasSize, int filterSize) {
        mRssiManager.setDeltasSize(deltasSize);
        mRssiManager.setFilterSize(filterSize);
    }

    /**
     * Throw away the old components
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