package edu.drexel.lapcounter.lapcounter.backend.ble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import java.util.List;

import edu.drexel.lapcounter.lapcounter.backend.wrappers.ContextWrapper;

public class BLEScanner implements DeviceScanner {
    private static final String TAG = BLEScanner.class.getSimpleName();
    private List<String> mWhitelist;
    private BLEService mBleService;
    private IContext mParentContext;
    private BluetoothAdapter.LeScanCallback mLeScanCallback;

    boolean mIsScanning = false;

    private ServiceConnection mBleServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            BLEService.LocalBinder binder = (BLEService.LocalBinder) service;
            mBleService = binder.getService();
            mBleService.startScan(mLeScanCallback);
        }

        public void onServiceDisconnected(ComponentName arg0) {
            mBleService = null;
        }
    };

    public BLEScanner(Context parent) {
        this(new ContextWrapper(parent));
    }

    public BLEScanner(IContext parent) {
        mParentContext = parent;
    }

    @Override
    public void setCallback(final Callback callback) {
        mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
            @Override
            public void onLeScan(final BluetoothDevice device, final int rssi, byte[] scanRecord) {
                String address = device.getAddress();

                if (mWhitelist == null || mWhitelist.contains(address)) {
                    callback.onDeviceFound(device.getName(), device.getAddress(), rssi);
                }
            }
        };
    }

    @Override
    public void setAddressWhitelist(List<String> allowedAddresses) {
        mWhitelist = allowedAddresses;
    }

    @Override
    public void startScan() {
        Intent intent = new Intent(mParentContext.getInner(), BLEService.class);
        mParentContext.bindService(intent, mBleServiceConnection, Context.BIND_AUTO_CREATE);
        mIsScanning = true;
    }

    @Override
    public void stopScan() {
        if (!mIsScanning) {
            Log.w(TAG, "stopScan() called when mIsScanning == false.");
            return;
        }

        mBleService.stopScan(mLeScanCallback);
        mParentContext.unbindService(mBleServiceConnection);
        mIsScanning = false;
    }

    // testing methods
    public BluetoothAdapter.LeScanCallback getmLeScanCallback() {
        return mLeScanCallback;
    }

    public List<String> getmWhitelist() {
        return mWhitelist;
    }

    void setmBleService(BLEService mBleService) {
        this.mBleService = mBleService;
    }

    public ServiceConnection getmBleServiceConnection() {
        return mBleServiceConnection;
    }
}
