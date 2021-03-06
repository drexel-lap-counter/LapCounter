package edu.drexel.lapcounter.lapcounter.backend.wrappers;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.content.Context;

import edu.drexel.lapcounter.lapcounter.backend.ble.IBluetoothAdapter;
import edu.drexel.lapcounter.lapcounter.backend.ble.IBluetoothDevice;

public class BluetoothAdapterWrapper implements IBluetoothAdapter {
    private final BluetoothAdapter mAdapter;

    public BluetoothAdapterWrapper(BluetoothAdapter adapter) {
        mAdapter = adapter;
    }

    @Override
    public IBluetoothDevice getRemoteDevice(final String address) {
        final BluetoothDevice device = mAdapter.getRemoteDevice(address);

        if (device == null) {
            return null;
        }

        return new IBluetoothDevice() {
            @Override
            public BluetoothGatt connectGatt(Context parent, boolean shouldReconnect,
                                             BluetoothGattCallback callback) {
                return device.connectGatt(parent, shouldReconnect, callback);
            }
        };
    }

    @Override
    public void startLeScan(BluetoothAdapter.LeScanCallback scanCallback) {
        mAdapter.startLeScan(scanCallback);
    }

    @Override
    public void stopLeScan(BluetoothAdapter.LeScanCallback scanCallback) {
        mAdapter.stopLeScan(scanCallback);
    }
}
