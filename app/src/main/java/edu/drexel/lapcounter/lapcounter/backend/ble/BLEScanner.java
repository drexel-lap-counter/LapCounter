package edu.drexel.lapcounter.lapcounter.backend.ble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import java.util.List;

public class BLEScanner implements DeviceScanner {
    private List<String> mWhitelist;
    private BLEService mBleService;
    private Context mParentContext;
    private BluetoothAdapter.LeScanCallback mLeScanCallback;

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
        Intent intent = new Intent(mParentContext, BLEService.class);
        mParentContext.bindService(intent, mBleServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void stopScan() {
        mBleService.stopScan(mLeScanCallback);
        mParentContext.unbindService(mBleServiceConnection);
    }
}
