package edu.drexel.lapcounter.lapcounter.backend.ble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.Objects;

import edu.drexel.lapcounter.lapcounter.backend.wrappers.BluetoothAdapterWrapper;
import edu.drexel.lapcounter.lapcounter.backend.wrappers.LocalBroadcastManagerWrapper;

/**
 * A low-level class to interface with Android's Bluetooth Low Energy stack.
 * This class provides functionality to connect, disconnect, and scan devices, as well as read
 * their Received Signal Strength Indicator (RSSI) values.
 *
 * This class publishes the underlying connection events used by other backend components.
 */
public class BLEComm {

    /**
     * The tag used to identify this class in execution logs.
     */
    private static final String TAG = BLEComm.class.getSimpleName();

    // Unique IDs for the Intents this server publishes
    /**
     * The action of the Intent that BLEComm publishes after a device connection.
     */
    public final static String ACTION_CONNECTED = qualify("ACTION_CONNECTED");

    /**
     * The action of the Intent that BLEComm publishes after the same device reconnects.
     */
    public final static String ACTION_RECONNECTED = qualify("ACTION_RECONNECTED");

    /**
     * The action of the Intent that BLEComm publishes after a device disconnection.
     */
    public final static String ACTION_DISCONNECTED = qualify("ACTION_DISCONNECTED");

    /**
     * The action of the Intent that BLEComm publishes after receiving a signal strength value.
     */
    public final static String ACTION_RAW_RSSI_AVAILABLE = qualify("ACTION_RAW_RSSI_AVAILABLE");

    /**
     * The extra containing the RSSI data in the Intent payload
     */
    public final static String EXTRA_RAW_RSSI = qualify("EXTRA_RAW_RSSI");

    /**
     * The extra in a disconnect event signifying whether the disconnection was explicitly
     * requested.
     */
    public final static String EXTRA_DISCONNECT_IS_INTENTIONAL =
            qualify("EXTRA_DISCONNECT_IS_INTENTIONAL");


    /**
     * The extra in a connect event containing the connected device's MAC address.
     */
    public final static String EXTRA_DEVICE_ADDRESS = qualify("EXTRA_DEVICE_ADDRESS");


    /**
     * Connection state when no device is connected.
     */
    static final int STATE_DISCONNECTED = 0;


    /**
     * Connection state when attempting to connect to a device.
     */
    static final int STATE_CONNECTING = 1;

    /**
     * Connection state when connected to a device.
     */
    static final int STATE_CONNECTED = 2;


    /**
     * An Intent-publishing manager to send Intents across Activities and Services.
     */
    private final IBroadcastManager mBroadcastManager;


    /**
     * The MAC address of the previously connected device, or null if no device has yet connected.
     */
    private String mPreviousConnectAddress;


    /**
     * The MAC address of the currently connected device, or null if no device is connected.
     */
    private String mCurrentConnectAddress;


    /**
     * An adapter to construct BluetoothDevice objects and to scan for devices.
     */
    private IBluetoothAdapter mBluetoothAdapter;


    /**
     * The underlying Android BLE server to communicate with the phone's BLE hardware.
     */
    private BluetoothGatt mBluetoothGatt;


    /**
     * The current connection state.
     *
     * @see #getConnectionState()
     */
    private int mConnectionState = STATE_DISCONNECTED;

    /**
     * BLEcomm requires a Context to request BLE permissions and to send Intents.
     */
    private final Context mParent;


    /**
     * A flag to keep track of whether the last disconnect was explicitly requested by the app
     * (as opposed to a disconnect caused by the device moving out-of-range).
     */
    private boolean mIntentionalDisconnect = false;


    /**
     * Callbacks for hardware BLE events, i.e., connection/disconnection, read RSSI.
     * These callbacks are responsible for publishing the corresponding Intents that other backend
     * components listen for.
     */
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
                broadcastDisconnect();

                mIntentionalDisconnect = false;
            }
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d("BLE_RSSI", "Callback called successfully " + Integer.toString(rssi));
                broadcastUpdate(rssi);
            } else {
                Log.w("BLE_RSSI", "Could not read remote RSSI!");
            }
        }

    };

    /**
     * @param s the identifier to prepend with the qualified package name.
     * @return the identifier fully-qualified with package information.
     */
    private static String qualify(String s) {
        Package p = Objects.requireNonNull(BLEComm.class.getPackage());
        return p.getName() + "." + s;
    }

    /**
     *  Construct a BLEComm with a reference to its Context owner.
     *
     * @param parent BLEcomm requires a Context to request BLE permissions and to send Intents.
     */
    public BLEComm(Context parent) {
        this(parent, new BluetoothAdapterWrapper(getAdapter(parent)),
                LocalBroadcastManagerWrapper.getInstance(parent));
    }

    /**
     * Construct a BLEComm with core dependencies injected. Used for unit testing this class.
     *
     * @param parent BLEcomm requires a Context to request BLE permissions and to send Intents.
     * @param adapter an adapter to construct BluetoothDevice objects and to scan for devices.
     * @param broadcastManager an Intent-publishing manager to send Intents across Activities and
     *                         services.
     */
    public BLEComm(Context parent, IBluetoothAdapter adapter, IBroadcastManager broadcastManager){
        mParent = parent;
        setBluetoothAdapter(adapter);
        mBroadcastManager = broadcastManager;
    }


    /**
     * @param c the Context that makes the request to the Android OS for access to the underlying
     *          Bluetooth adapter.
     * @return an instance of the underlying Bluetooth adapter, or null if the app cannot access
     * the BLE hardware, e.g., lack of permissions, lack of hardware support.
     */
    private static BluetoothAdapter getAdapter(Context c) {
        BluetoothManager m = (BluetoothManager) c.getSystemService(Context.BLUETOOTH_SERVICE);
        return m.getAdapter();
    }


    /**
     * Notify the underlying BLE stack to read an RSSI value from the connected device.
     * If the read operation is successful, then BLEComm will publish an event.
     *
     * @see #ACTION_RAW_RSSI_AVAILABLE
     * @see #EXTRA_RAW_RSSI
     */
    public void requestRssi() {
        if (mConnectionState == STATE_CONNECTED) {
            mBluetoothGatt.readRemoteRssi();
        }
    }

    /**
     * Broadcast an Intent locally in the app.
     *
     * @param intent the Intent to locally broadcast.
     */
    private void localBroadcast(Intent intent) {
        mBroadcastManager.sendBroadcast(intent);
    }


    /**
     * Locally broadcast a connection event to the app.
     * The extra {@link #EXTRA_DEVICE_ADDRESS} contains the MAC address of the connected device.
     *
     * @param isReconnect whether the connection event refers to a reconnection.
     * @see #ACTION_CONNECTED
     * @see #ACTION_RECONNECTED
     */
    private void broadcastConnect(boolean isReconnect) {
        String action = isReconnect ? ACTION_RECONNECTED : ACTION_CONNECTED;
        Intent intent = new Intent(action);
        intent.putExtra(EXTRA_DEVICE_ADDRESS, mCurrentConnectAddress);
        localBroadcast(intent);
    }

    /**
     * Locally broadcast a disconnect event to the app.
     * The extra {@link #EXTRA_DISCONNECT_IS_INTENTIONAL} contains whether the disconnect event
     * was explicitly requested the app.
     *
     * @see #ACTION_DISCONNECTED
     */
    private void broadcastDisconnect() {
        final Intent intent = new Intent(BLEComm.ACTION_DISCONNECTED);
        intent.putExtra(EXTRA_DISCONNECT_IS_INTENTIONAL, mIntentionalDisconnect);
        localBroadcast(intent);
    }

    /**
     * Locally broadcast an RSSI value to the app.
     * The extra {@link #EXTRA_RAW_RSSI} contains the RSSI value.
     *
     * @param rssi the RSSI value to broadcast.
     * @see #ACTION_RAW_RSSI_AVAILABLE
     */
    private void broadcastUpdate(int rssi) {
        final Intent intent = new Intent(BLEComm.ACTION_RAW_RSSI_AVAILABLE);
        intent.putExtra(EXTRA_RAW_RSSI, rssi);
        localBroadcast(intent);
    }

    /**
     * Connect to a BLE device.
     *
     * @param address the MAC address of the BLE device to connect to.
     * @return return true if the connection is initiated successfully. The connection result
     * is reported asynchronously through an Intent with action {@link #ACTION_CONNECTED}.
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
     * Disconnect from the connected device and release internal resources.
     *
     * @see #connect(String)
     * @see #ACTION_DISCONNECTED
     * @see #EXTRA_DISCONNECT_IS_INTENTIONAL
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

    /**
     * Begin scanning for BLE devices.
     *
     * @param scanCallback the callback to invoke when devices are found.
     */
    void startScan(BluetoothAdapter.LeScanCallback scanCallback) {
        mBluetoothAdapter.startLeScan(scanCallback);
    }

    /**
     * Stop scanning for BLE devices.
     *
     * @param scanCallback the callback previously used when starting a scan.
     * @see #startScan(BluetoothAdapter.LeScanCallback)
     */
    void stopScan(BluetoothAdapter.LeScanCallback scanCallback) {
        mBluetoothAdapter.stopLeScan(scanCallback);
    }

    /**
     * @return the current connection state of the device.
     *
     * @see #STATE_CONNECTED
     * @see #STATE_CONNECTING
     * @see #STATE_DISCONNECTED
     */
    int getConnectionState() {
        return mConnectionState;
    }

    /**
     * Resets internal state. Consumers of this class should call this method when
     * the device's subsequent connection event should be broadcast as a {@link #ACTION_CONNECTED}
     * instead of as a {@link #ACTION_RECONNECTED}.
     */
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
