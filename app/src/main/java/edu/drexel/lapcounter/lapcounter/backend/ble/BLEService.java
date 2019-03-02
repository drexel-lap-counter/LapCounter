package edu.drexel.lapcounter.lapcounter.backend.ble;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.IBinder;

import edu.drexel.lapcounter.lapcounter.backend.SimpleMessageReceiver;

public class BLEService extends Service {
    private BLEComm mBleComm;
    private RSSIManager mRssiManager;

    private final IBinder mBinder = new LocalBinder();

    private final SimpleMessageReceiver mReceiver = new SimpleMessageReceiver();

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
        mRssiManager = new RSSIManager(this, mBleComm);
        mRssiManager.initCallbacks(mReceiver);
        mReceiver.attach(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mReceiver.detach(this);
    }

    public void startScan(BluetoothAdapter.LeScanCallback scanCallback) {
        mBleComm.startScan(scanCallback);
    }

    public void stopScan(BluetoothAdapter.LeScanCallback scanCallback) {
        mBleComm.stopScan(scanCallback);
    }

    public void connect(String deviceAddress) {
        mBleComm.connect(deviceAddress);
    }

    public void disconnect() {
        mBleComm.close();
    }

    public void startRssiRequests() {
        mRssiManager.scheduleRssiRequest();
    }

    public void stopRssiRequests() {
        mRssiManager.stopRssiRequests();
    }
}