package edu.drexel.lapcounter.lapcounter.backend;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

// TODO: BLEComm
public class BLEService extends Service {
    // Tag for logging
    public static final String TAG = BLEService.class.getSimpleName();

    // States of connection
    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    // These are needed for making connections
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;

    // The GATT server. Most of this service interacts with this object
    private BluetoothGatt mBluetoothGatt;
    private int mConnectionState = STATE_DISCONNECTED;

    // Unique IDs for the Intents this server publishes
    public final static String ACTION_DEVICE_CONNECTED =
            "edu.drexel.lapcounter.lapcounter.backend.ACTION_DEVICE_CONNECTED";
    public final static String ACTION_DEVICE_DISCONNECTED =
            "edu.drexel.lapcounter.lapcounter.backend.ACTION_DEVICE_DISCONNECTED";
    public final static String ACTION_RAW_RSSI_AVAILABLE =
            "edu.drexel.lapcounter.lapcounter.backend.ACTION_RAW_RSSI_AVAILABLE";

    // Tag for the RSSI data in the Intent payload
    public final static String EXTRA_RAW_RSSI =
            "edu.drexel.lapcounter.lapcounter.backend.EXTRA_RAW_RSSI";

    // Callback for GATT serverevents.
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            // Publish a connect/disconnect message
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                mConnectionState = STATE_CONNECTED;
                Log.d(TAG, "Connected to GATT server. status = " + status);
                broadcastUpdate(ACTION_DEVICE_CONNECTED);
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                mConnectionState = STATE_DISCONNECTED;
                Log.d(TAG, "Disconnected from GATT server. status = " + status);
                broadcastUpdate(ACTION_DEVICE_DISCONNECTED);
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

    private final IBinder mBinder = new LocalBinder();

    public class LocalBinder extends Binder {
        public BLEService getService() {
            return BLEService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // Make sure we close our Bluetooth connections
        close();
        return super.onUnbind(intent);
    }

    /**
     * Initializes a reference to the local Bluetooth adapter.
     *
     * @return Return true if the initialization is successful.
     */
    public boolean initialize() {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }

        return true;
    }

    /**
     * Use the GATT object to request an update to the RSSI
     */
    public void requestRssi() {
        if (mConnectionState == STATE_CONNECTED) {
            mBluetoothGatt.readRemoteRssi();
        }
    }

    private void localBroadcast(Intent intent) {
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    /**
     * Broadcast an an intent for device connected/disconnected
     * @param action
     */
    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        localBroadcast(intent);
    }

    /**
     * Broadcast an intent for device RSSI update
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
     *
     * @return Return true if the connection is initiated successfully. The connection result
     *         is reported asynchronously through the
     *         {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     *         callback.
     */
    public boolean connect(final String address) {
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
        close();

        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        mBluetoothGatt = device.connectGatt(this, false, mGattCallback);

        Log.d(TAG, "Trying to create a new connection.");
        mConnectionState = STATE_CONNECTING;
        return true;
    }

    /**
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }
}
