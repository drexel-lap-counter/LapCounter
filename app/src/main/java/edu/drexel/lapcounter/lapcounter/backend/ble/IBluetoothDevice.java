package edu.drexel.lapcounter.lapcounter.backend.ble;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.content.Context;

public interface IBluetoothDevice {
    BluetoothGatt connectGatt(Context parent, boolean shouldReconnect,
                              BluetoothGattCallback callback);
}
