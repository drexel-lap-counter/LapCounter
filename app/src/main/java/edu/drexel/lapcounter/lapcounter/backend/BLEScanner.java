package edu.drexel.lapcounter.lapcounter.backend;

import android.bluetooth.BluetoothDevice;

import java.util.List;

public interface BLEScanner {

    /**
     * Since Bluetooth scanning is asynchronous, the user of this interface must specify
     * a callback for processing a new device.
     */
    interface Callback {
        /**
         * This callback will be called once per device detected
         * @param deviceName the name of the device
         * @param deviceAddress the addreess of the device
         * @param rssi the rssi value detected at *scan time*. This might not be needed, but
         *             it might be useful for selecting the correct device
         */
        void onDeviceFound(String deviceName, String deviceAddress, int rssi);
    }

    /**
     * Set a callback that is c
     * @param callback the callback for processing each device found
     */
    void setCallback(Callback callback);

    /**
     * For scanning for only registered devices, pass in a list of addresses that are
     * valid. This way, we
     * @param allowedAddresses A list of MAC addresses that are registered to be used
     *                         for lap counting. Only scan results that are in this list
     *                         trigger the caallback
     */
    void setAddressWhitelist(List<String> allowedAddresses);

    /**
     * Call this to start a scan.
     */
    void startScan();

    /**
     * Stop the current scan
     */
    void stopScan();
}
