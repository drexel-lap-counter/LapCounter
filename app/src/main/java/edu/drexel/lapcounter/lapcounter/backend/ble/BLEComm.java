package edu.drexel.lapcounter.lapcounter.backend.ble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.Objects;

import edu.drexel.lapcounter.lapcounter.backend.wrappers.BluetoothAdapterWrapper;
import edu.drexel.lapcounter.lapcounter.backend.wrappers.LocalBroadcastManagerWrapper;

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
    public final static String EXTRA_DISCONNECT_IS_INTENTIONAL = qualify("EXTRA_DISCONNECT_IS_INTENTIONAL");

    public final static String EXTRA_DEVICE_ADDRESS = qualify("EXTRA_DEVICE_ADDRESS");


    // States of connection
    static final int STATE_DISCONNECTED = 0;
    static final int STATE_CONNECTING = 1;
    static final int STATE_CONNECTED = 2;
    private final IBroadcastManager mBroadcastManager;

    private String mPreviousConnectAddress;
    private String mCurrentConnectAddress;

    private IBluetoothAdapter mBluetoothAdapter;
    private BluetoothGatt mBluetoothGatt;

    private int mConnectionState = STATE_DISCONNECTED;

    private final Context mParent;

    private boolean mIntentionalDisconnect = false;

    // Callback for GATT server events.
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
                broadcastDisconnect(ACTION_DISCONNECTED);

                mIntentionalDisconnect = false;
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
        this(parent, new BluetoothAdapterWrapper(getAdapter(parent)),
                new LocalBroadcastManagerWrapper(LocalBroadcastManager.getInstance(parent)));
    }

    public BLEComm(Context parent, IBluetoothAdapter adapter, IBroadcastManager broadcastManager){
        mParent = parent;
        setBluetoothAdapter(adapter);
        mBroadcastManager = broadcastManager;
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
        mBroadcastManager.sendBroadcast(intent);
    }

    private void broadcastConnect(boolean isReconnect) {
        String action = isReconnect ? ACTION_RECONNECTED : ACTION_CONNECTED;
        Intent intent = new Intent(action);
        intent.putExtra(EXTRA_DEVICE_ADDRESS, mCurrentConnectAddress);
        localBroadcast(intent);
    }

    /**
     * Broadcast an an intent for device connected/disconnected
     *
     * @param action
     */
    private void broadcastDisconnect(final String action) {
        final Intent intent = new Intent(action);
        intent.putExtra(EXTRA_DISCONNECT_IS_INTENTIONAL, mIntentionalDisconnect);
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

        IBluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);

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
        mIntentionalDisconnect = true;
    }

    void startScan(BluetoothAdapter.LeScanCallback scanCallback) {
        disconnect();
        mBluetoothAdapter.startLeScan(scanCallback);
    }

    void stopScan(BluetoothAdapter.LeScanCallback scanCallback) {
        mBluetoothAdapter.stopLeScan(scanCallback);
    }

    int getConnectionState() {
        return mConnectionState;
    }

    void reset() {
        mPreviousConnectAddress = null;
        mCurrentConnectAddress = null;
        mIntentionalDisconnect = false;
    }

    // Methods for testing

    public BluetoothGatt getmBluetoothGatt() {
        return mBluetoothGatt;
    }

    public void setmBluetoothGatt(BluetoothGatt mBluetoothGatt) {
        this.mBluetoothGatt = mBluetoothGatt;
    }

    public void setmConnectionState(int mConnectionState) {
        this.mConnectionState = mConnectionState;
    }

    public void setBluetoothAdapter(BluetoothAdapter adapter) {
        if (adapter == null) {
            setBluetoothAdapter((IBluetoothAdapter)null);
        } else {
            setBluetoothAdapter(new BluetoothAdapterWrapper(adapter));
        }
    }

    public void setBluetoothAdapter(IBluetoothAdapter adapter) {
        this.mBluetoothAdapter = adapter;
    }

    public String getmPreviousConnectAddress() {
        return mPreviousConnectAddress;
    }

    public String getmCurrentConnectAddress() {
        return mCurrentConnectAddress;
    }
}
