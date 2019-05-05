package edu.drexel.lapcounter.lapcounter.backend.ble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

class BluetoothAdapterWrapped implements IBluetoothAdapter {
    private final BluetoothAdapter mAdapter;

    public BluetoothAdapterWrapped(BluetoothAdapter adapter) {
        mAdapter = adapter;
    }

    @Override
    public BluetoothDevice getRemoteDevice(String address) {
        return mAdapter.getRemoteDevice(address);
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
