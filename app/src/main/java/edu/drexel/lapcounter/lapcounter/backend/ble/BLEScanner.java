package edu.drexel.lapcounter.lapcounter.backend.ble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import java.util.List;

import edu.drexel.lapcounter.lapcounter.backend.wrappers.ContextWrapper;

/**
 * A class to facilitate scanning for Bluetooth Low Energy devices.
 *
 * @see BLEComm
 */
public class BLEScanner implements DeviceScanner {
    /**
     * The tag used to identify this class in execution logs.
     */
    private static final String TAG = BLEScanner.class.getSimpleName();

    /**
     * The collection of MAC addresses of found devices to report back to the consumers of this
     * class. Found devices without MAC addresses in this collection will not be reported.
     *
     * If this collection is null, then BLEScanner will report all found devices.
     * @see DeviceScanner#setAddressWhitelist(List)
     */
    private List<String> mWhitelist;


    /**
     * The Service interface to {@link BLEComm}.
     */
    private BLEService mBleService;


    /**
     * BLEScanner requires a Context to send Intents and to bind to Services.
     */
    private IContext mParentContext;


    /**
     * The user-supplied {@link DeviceScanner.Callback} wrapped in Android's underlying scanner
     * callback object.
     */
    private BluetoothAdapter.LeScanCallback mLeScanCallback;

    /**
     * A flag to indicate whether this scanner is currently looking for BLE devices.
     */
    boolean mIsScanning = false;

    /**
     * The Service connection/disconnection callbacks attached to {@link #mBleService}.
     */
    private ServiceConnection mBleServiceConnection = new ServiceConnection() {
        /**
         * {@inheritDoc}
         * On Service connection, BLEScanner will start scanning for BLE devices.
         */
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            BLEService.LocalBinder binder = (BLEService.LocalBinder) service;
            mBleService = binder.getService();
            mBleService.startScan(mLeScanCallback);
        }

        public void onServiceDisconnected(ComponentName arg0) {
            mBleService = null;
        }
    };

    /** Construct a BLEScanner with a reference to its owner.
     * @param parent BLEScanner requires a Context to send Intents and to bind to Services.
     */
    public BLEScanner(Context parent) {
        this(new ContextWrapper(parent));
    }


    /** Construct a BLEScanner with a reference to its owner.
     * @param parent BLEScanner requires a Context to send Intents and to bind to Services.
     */
    public BLEScanner(IContext parent) {
        mParentContext = parent;
    }

    /**
     * @param callback The callback the receives information about found devices.
     */
    @Override
    public void setCallback(final Callback callback) {
        mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
            @Override
            public void onLeScan(final BluetoothDevice device, final int rssi, byte[] scanRecord) {
                String address = device.getAddress();

                if (mWhitelist == null || mWhitelist.contains(address)) {
                    callback.onDeviceFound(device.getName(), device.getAddress(), rssi);
                }
            }
        };
    }

    @Override
    public void setAddressWhitelist(List<String> allowedAddresses) {
        mWhitelist = allowedAddresses;
    }

    @Override
    public void startScan() {
        Intent intent = new Intent(mParentContext.getInner(), BLEService.class);
        mParentContext.bindService(intent, mBleServiceConnection, Context.BIND_AUTO_CREATE);
        mIsScanning = true;
    }

    @Override
    public void stopScan() {
        if (!mIsScanning) {
            Log.w(TAG, "stopScan() called when mIsScanning == false.");
            return;
        }

        mBleService.stopScan(mLeScanCallback);
        mParentContext.unbindService(mBleServiceConnection);
        mIsScanning = false;
    }

    // testing methods
    public BluetoothAdapter.LeScanCallback getmLeScanCallback() {
        return mLeScanCallback;
    }

    public List<String> getmWhitelist() {
        return mWhitelist;
    }
}
