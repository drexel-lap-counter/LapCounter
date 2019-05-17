package edu.drexel.lapcounter.lapcounter.backend.ble;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.content.Context;

/**
 * Wrapper around a BluetoothDevice for use in unit testing. This makes it easier to mock
 * classes that rely on Android system code which is often notoriously hard to mock.
 */
public interface IBluetoothDevice {
    /**
     * Connect to the GATT server
     * @param parent the parent Context
     * @param shouldReconnect true if we should reconnect to the device
     * @param callback the callback for when the GATT server connects
     * @return a reference to the GATT server.
     */
    BluetoothGatt connectGatt(
            Context parent, boolean shouldReconnect, BluetoothGattCallback callback);
}
