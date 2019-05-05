package edu.drexel.lapcounter.lapcounter.backend.ble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

interface IBluetoothAdapter {
    BluetoothDevice getRemoteDevice(String address);

    void startLeScan(BluetoothAdapter.LeScanCallback scanCallback);

    void stopLeScan(BluetoothAdapter.LeScanCallback scanCallback);
}
