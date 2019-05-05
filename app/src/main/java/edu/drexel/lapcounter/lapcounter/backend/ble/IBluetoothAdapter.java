package edu.drexel.lapcounter.lapcounter.backend.ble;

import android.bluetooth.BluetoothAdapter;

public interface IBluetoothAdapter {
    IBluetoothDevice getRemoteDevice(String address);

    void startLeScan(BluetoothAdapter.LeScanCallback scanCallback);

    void stopLeScan(BluetoothAdapter.LeScanCallback scanCallback);
}
