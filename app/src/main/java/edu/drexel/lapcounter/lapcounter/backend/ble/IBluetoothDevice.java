package edu.drexel.lapcounter.lapcounter.backend.ble;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.content.Context;

interface IBluetoothDevice {
    BluetoothGatt connectGatt(Context mParent, boolean b, BluetoothGattCallback mGattCallback);
}
