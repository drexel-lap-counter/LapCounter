package edu.drexel.lapcounter.lapcounter.backend.ble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import java.util.List;

import edu.drexel.lapcounter.lapcounter.backend.DeviceScanner;

public class BLEScanner implements DeviceScanner {
    private List<String> mWhitelist;
    private BLEComm mBleComm;
    private Context mParentContext;
    private BluetoothAdapter.LeScanCallback mLeScanCallback;

    private ServiceConnection mBleCommConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            BLEComm.LocalBinder binder = (BLEComm.LocalBinder) service;
            mBleComm = binder.getService();
            mBleComm.startScan(mLeScanCallback);
        }

        public void onServiceDisconnected(ComponentName arg0) {
            mBleComm = null;
        }
    };

    public BLEScanner(Context parent) {
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
        Intent intent = new Intent(mParentContext, BLEComm.class);
        mParentContext.bindService(intent, mBleCommConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void stopScan() {
        mBleComm.stopScan(mLeScanCallback);
        mParentContext.unbindService(mBleCommConnection);
    }
}
