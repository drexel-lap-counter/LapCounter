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
    private final RSSIManager mRssiManager = new RSSIManager(this);

    private final IBinder mBinder = new LocalBinder();

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

    public double getRssi() {
        return mRssiManager.getRssi();
    }

    public int getDirection() {
        return mRssiManager.getDirection();
    }

    public void clearRssiManager() {
        mRssiManager.clear();
    }

    public void startScan(BluetoothAdapter.LeScanCallback scanCallback) {
        mBleComm.startScan(scanCallback);
    }

    public void stopScan(BluetoothAdapter.LeScanCallback scanCallback) {
        mBleComm.stopScan(scanCallback);
    }

    public boolean connect(String deviceAddress) {
        return mBleComm.connect(deviceAddress);
    }

    public void disconnect() {
        mBleComm.close();
    }
}