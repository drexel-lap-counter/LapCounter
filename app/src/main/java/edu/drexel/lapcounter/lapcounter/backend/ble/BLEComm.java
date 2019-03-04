package edu.drexel.lapcounter.lapcounter.backend.ble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.Objects;

public class BLEComm {
    // Tag for logging
    private static final String TAG = BLEComm.class.getSimpleName();

    // Unique IDs for the Intents this server publishes
    public final static String ACTION_CONNECTED = qualify("ACTION_CONNECTED");
    public final static String ACTION_RECONNECTED = qualify("ACTION_RECONNECTED");
    public final static String ACTION_DISCONNECTED = qualify("ACTION_DISCONNECTED");
    public final static String ACTION_RAW_RSSI_AVAILABLE = qualify("ACTION_RAW_RSSI_AVAILABLE");

    // Tag for the RSSI data in the Intent payload
    public final static String EXTRA_RAW_RSSI = qualify("EXTRA_RAW_RSSI");

    // States of connection
    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    static final int STATE_CONNECTED = 2;

    private String mPreviousConnectAddress;
    private String mCurrentConnectAddress;

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGatt mBluetoothGatt;

    private int mConnectionState = STATE_DISCONNECTED;

    private final Context mParent;

    // Callback for GATT serverevents.
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            // Publish a connect/disconnect message
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                mConnectionState = STATE_CONNECTED;
                Log.d(TAG, "Connected to GATT server. status = " + status);

                boolean isReconnect = mCurrentConnectAddress.equals(mPreviousConnectAddress);
                broadcastConnect(isReconnect);
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                mConnectionState = STATE_DISCONNECTED;
                Log.d(TAG, "Disconnected from GATT server. status = " + status);
                broadcastUpdate(ACTION_DISCONNECTED);
            }
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d("BLE_RSSI", "Callback called successfully " + Integer.toString(rssi));
                broadcastUpdate(ACTION_RAW_RSSI_AVAILABLE, rssi);
            } else {
                Log.w("BLE_RSSI", "Could not read remote RSSI!");
            }
        }

    };

    private static String qualify(String s) {
        Package p = Objects.requireNonNull(BLEComm.class.getPackage());
        return p.getName() + "." + s;
    }

    public BLEComm(Context parent) {
        mParent = parent;
        mBluetoothAdapter = getAdapter(mParent);
    }

    private static BluetoothAdapter getAdapter(Context c) {
        BluetoothManager m = (BluetoothManager) c.getSystemService(Context.BLUETOOTH_SERVICE);
        return m.getAdapter();
    }

    /**
     * Use the GATT object to request an update to the RSSI
     */
    void requestRssi() {
        if (mConnectionState == STATE_CONNECTED) {
            mBluetoothGatt.readRemoteRssi();
        }
    }

    private void localBroadcast(Intent intent) {
        LocalBroadcastManager.getInstance(mParent).sendBroadcast(intent);
    }

    private void broadcastConnect(boolean isReconnect) {
        String action = isReconnect ? ACTION_RECONNECTED : ACTION_CONNECTED;
        localBroadcast(new Intent(action));
    }

    /**
     * Broadcast an an intent for device connected/disconnected
     *
     * @param action
     */
    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        localBroadcast(intent);
    }

    /**
     * Broadcast an intent for device RSSI update
     *
     * @param action
     * @param rssi
     */
    private void broadcastUpdate(final String action, int rssi) {
        final Intent intent = new Intent(action);
        intent.putExtra(EXTRA_RAW_RSSI, rssi);
        localBroadcast(intent);
    }

    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @param address The device address of the destination device.
     * @return Return true if the connection is initiated successfully. The connection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    boolean connect(final String address) {
        if (mBluetoothAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        if (mConnectionState == STATE_CONNECTING) {
            // We've already made a connect request.
            Log.d(TAG, "connect() - returned early because mConnectionState == STATE_CONNECTING");
            return true;
        }

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);

        if (device == null) {
            Log.w(TAG, "Device not found.  Unable to connect.");
            return false;
        }

        // Release resources for a previously instantiated mBluetoothGatt.
        disconnect();

        mPreviousConnectAddress = mCurrentConnectAddress;
        mCurrentConnectAddress = address;

        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        mBluetoothGatt = device.connectGatt(mParent, false, mGattCallback);

        Log.d(TAG, "Trying to create a new connection.");
        mConnectionState = STATE_CONNECTING;
        return true;
    }

    /**
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    void disconnect() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
        mConnectionState = STATE_DISCONNECTED;
    }

    void startScan(BluetoothAdapter.LeScanCallback scanCallback) {
        mBluetoothAdapter.startLeScan(scanCallback);
    }

    void stopScan(BluetoothAdapter.LeScanCallback scanCallback) {
        mBluetoothAdapter.stopLeScan(scanCallback);
    }

    int getConnectionState() {
        return mConnectionState;
    }
}