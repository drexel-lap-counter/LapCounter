package edu.drexel.lapcounter.lapcounter.backend.ble;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.IBinder;

public class BLEService extends Service {
    private BLEComm mBleComm;
    private RSSIManager mRssiManager;

    private final IBinder mBinder = new LocalBinder();

    private boolean mShouldScan = false;
    private BluetoothAdapter.LeScanCallback mScanCallback;

    private boolean mShouldConnect = false;
    private String mDeviceAddress;

    public class LocalBinder extends Binder {
        public BLEService getService() {
            return BLEService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private ServiceConnection mBleCommConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            BLEComm.LocalBinder binder = (BLEComm.LocalBinder) service;
            mBleComm = binder.getService();
            mRssiManager = new RSSIManager(BLEService.this, mBleComm);

            if (mShouldScan) {
                mBleComm.startScan(mScanCallback);
            } else if (mShouldConnect) {
                connect();
            }
        }

        public void onServiceDisconnected(ComponentName arg0) {
            mBleComm = null;
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        Intent intent = new Intent(this, BLEComm.class);
        bindService(intent, mBleCommConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onDestroy() {
        unbindService(mBleCommConnection);
        super.onDestroy();
    }

    public void startScan(BluetoothAdapter.LeScanCallback scanCallback) {
        if (mBleComm == null) {
            mShouldScan = true;
            mScanCallback = scanCallback;
        } else {
            mBleComm.startScan(scanCallback);
        }
    }

    public void stopScan(BluetoothAdapter.LeScanCallback scanCallback) {
        if (mBleComm != null) {
            mBleComm.stopScan(scanCallback);
        }

        mShouldScan = false;
        mScanCallback = null;
    }

    public void connect(String deviceAddress) {
        if (mBleComm == null) {
            mShouldConnect = true;
            mDeviceAddress = deviceAddress;
        } else {
            connect();
        }
    }

    private void connect() {
        mBleComm.connect(mDeviceAddress);
        mShouldConnect = false;
        mDeviceAddress = null;
    }

    public void disconnect() {
        mBleComm.close();
    }
}