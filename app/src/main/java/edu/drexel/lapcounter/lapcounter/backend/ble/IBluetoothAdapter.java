package edu.drexel.lapcounter.lapcounter.backend.ble;

import android.bluetooth.BluetoothAdapter;

/**
 * This is a wrapper around a BluetoothAdapter for use in unit testing. This adds a layer between
 * our code and Android code that is hard to mock.
 */
public interface IBluetoothAdapter {
    /**
     * Get a bluetooth device
     * @param address the MAC address of the deviice
     * @return a bluetooth device
     */
    IBluetoothDevice getRemoteDevice(String address);

    /**
     * Start scanning for Bluetooth devices
     * @param scanCallback a callback called once per device
     */
    void startLeScan(BluetoothAdapter.LeScanCallback scanCallback);

    /**
     * Stop scanning for bluetooth devices
     * @param scanCallback a callback called when the scan is finished.
     */
    void stopLeScan(BluetoothAdapter.LeScanCallback scanCallback);
}
