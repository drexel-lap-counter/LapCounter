package edu.drexel.lapcounter.lapcounter.backend.ble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import edu.drexel.lapcounter.lapcounter.backend.SimpleMessageReceiver;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class BLECommTest {

    private BLEComm comm;

    @Mock
    Context context;

    @Mock
    BluetoothGatt gatt;

    private SimpleMessageReceiver mReceiver;
    private IBroadcastManager mBroadcastManager;
    private MockDevice mDevice;
    private MockAdapter mAdapter;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        mReceiver = new SimpleMessageReceiver();

        mBroadcastManager = new IBroadcastManager() {
            @Override
            public void sendBroadcast(Intent intent) {
                // Feed the broadcast intent directly to SimpleMessageReceiver.
                mReceiver.onReceive(context, intent);
            }
        };

        mDevice = new MockDevice();
        mAdapter = new MockAdapter(mDevice);

        comm = new BLEComm(context, mAdapter, mBroadcastManager);
    }

    @Test
    public void requestRssi_calls_gatt_if_connected() {
        comm.setmConnectionState(BLEComm.STATE_CONNECTED);
        when(gatt.readRemoteRssi()).thenReturn(true);
        comm.setmBluetoothGatt(gatt);

        comm.requestRssi();

        verify(gatt, atLeastOnce()).readRemoteRssi();
    }

    @Test
    public void requestRssi_does_not_call_gatt_if_not_connected() {
        comm.setmConnectionState(3);
        when(gatt.readRemoteRssi()).thenReturn(true);
        comm.setmBluetoothGatt(gatt);

        comm.requestRssi();

        verify(gatt, times(0)).readRemoteRssi();
    }

    @Test
    public void connect_returns_false_if_adapter_is_null() {
        comm.setBluetoothAdapter((BluetoothAdapter) null);
        String address = "some address";

        boolean connect = comm.connect(address);

        assertFalse(connect);
    }

    @Test
    public void connect_returns_false_if_address_is_null() {
        boolean connect = comm.connect(null);

        assertFalse(connect);
    }

    @Test
    public void connect_returns_true_if_already_connecting() {
        comm.setmConnectionState(BLEComm.STATE_CONNECTING);
        String address = "some address";

        boolean connect = comm.connect(address);

        assertTrue(connect);
    }

    @Test
    public void connect_returns_false_if_device_is_null() {
        String address = "some address";
        BluetoothAdapter adapter = mock(BluetoothAdapter.class);
        when(adapter.getRemoteDevice(address)).thenReturn(null);
        comm.setBluetoothAdapter(adapter);

        boolean connect = comm.connect(address);

        assertFalse(connect);
    }

    @Test
    public void connect_returns_true_if_device_is_not_null() {
        boolean connect = comm.connect("device_address");
        assertTrue(connect);
    }

    @Test
    public void disconnect_sets_gatt_to_null() {
        comm.setmBluetoothGatt(gatt);

        comm.disconnect();

        assertNotNull(gatt);
        assertNull(comm.getmBluetoothGatt());
    }

    @Test
    public void startScan_calls_BluetoothAdapter_LEScan() {
        BluetoothAdapter.LeScanCallback callback = mock(BluetoothAdapter.LeScanCallback.class);
        BluetoothAdapter adapter = mock(BluetoothAdapter.class);
        comm.setBluetoothAdapter(adapter);

        comm.startScan(callback);

        verify(adapter, only()).startLeScan(callback);
    }

    @Test
    public void stopScan_calls_adapter_stopscan() {
        BluetoothAdapter.LeScanCallback callback = mock(BluetoothAdapter.LeScanCallback.class);
        BluetoothAdapter adapter = mock(BluetoothAdapter.class);
        comm.setBluetoothAdapter(adapter);

        comm.stopScan(callback);

        verify(adapter, only()).stopLeScan(callback);
    }

    @Test
    public void reset_nulls_previousConnectedAddress() {
        comm.connect("String1");
        comm.connect("String2");

        comm.reset();

        assertNull(comm.getmPreviousConnectAddress());
    }

    @Test
    public void reset_nulls_currentConnectedAddress() {
        comm.connect("String1");
        comm.connect("String2");

        comm.reset();

        assertNull(comm.getmCurrentConnectAddress());
    }

    @Test
    public void getConnectionState_gets_state() {
        int mConnectionState = 33;
        comm.setmConnectionState(mConnectionState);

        assertEquals(comm.getConnectionState(), mConnectionState);
    }

    class MockDevice implements IBluetoothDevice {
        private BluetoothGattCallback mCallback;

        @Override
        public BluetoothGatt connectGatt(Context parent, boolean shouldReconnect,
                                         BluetoothGattCallback callback) {
            mCallback = callback;
            return gatt;
        }

        public void sendConnected() {
            mCallback.onConnectionStateChange(gatt, -1, BluetoothProfile.STATE_CONNECTED);
        }

        public void sendDisconnected() {
            mCallback.onConnectionStateChange(gatt, -1, BluetoothProfile.STATE_DISCONNECTED);
        }
    }

    class MockAdapter implements IBluetoothAdapter {
        private final IBluetoothDevice mDevice;

        MockAdapter(IBluetoothDevice device) {
            mDevice = device;
        }

        @Override
        public IBluetoothDevice getRemoteDevice(String address) {
            return mDevice;
        }

        @Override
        public void startLeScan(BluetoothAdapter.LeScanCallback scanCallback) {
            scanCallback.onLeScan(null, -1, null);
        }

        @Override
        public void stopLeScan(BluetoothAdapter.LeScanCallback scanCallback) {
        }
    }

    @Test
    public void first_connect_changes_connection_state() {
        final MockDevice device = new MockDevice();
        final MockAdapter adapter = new MockAdapter(device);
        comm.setBluetoothAdapter(adapter);

        comm.connect("device_address");
        device.sendConnected();

        assertEquals(BLEComm.STATE_CONNECTED, comm.getConnectionState());
    }

    @Test
    public void reconnect_changes_connection_state() {
        final MockDevice device = new MockDevice();
        final MockAdapter adapter = new MockAdapter(device);
        comm.setBluetoothAdapter(adapter);

        comm.connect("device_address");
        device.sendConnected();

        // Reconnect
        comm.connect("device_address");
        device.sendConnected();

        assertEquals(BLEComm.STATE_CONNECTED, comm.getConnectionState());
    }

    @Test
    public void first_connect_broadcasts_connect_action() {
        final MockDevice device = new MockDevice();
        final MockAdapter adapter = new MockAdapter(device);
        comm.setBluetoothAdapter(adapter);

        SimpleMessageReceiver.MessageHandler onConnect = new SimpleMessageReceiver.MessageHandler() {
            @Override
            public void onMessage(Intent message) {
                assertEquals(BLEComm.ACTION_CONNECTED, message.getAction());
            }
        };

        mReceiver.registerHandler(BLEComm.ACTION_CONNECTED, onConnect);
        comm.connect("device_address");
        device.sendConnected();
    }

    @Test
    public void reconnect_broadcasts_reconnect_action() {
        final MockDevice device = new MockDevice();
        final MockAdapter adapter = new MockAdapter(device);
        comm.setBluetoothAdapter(adapter);

        SimpleMessageReceiver.MessageHandler onReconnect = new SimpleMessageReceiver.MessageHandler() {
            @Override
            public void onMessage(Intent message) {
                assertEquals(BLEComm.ACTION_RECONNECTED, message.getAction());
            }
        };

        mReceiver.registerHandler(BLEComm.ACTION_RECONNECTED, onReconnect);

        comm.connect("device_address");
        device.sendConnected();

        // Reconnect
        comm.connect("device_address");
        device.sendConnected();
    }

    @Test
    public void disconnect_broadcasts_disconnect_action() {
        final MockDevice device = new MockDevice();
        final MockAdapter adapter = new MockAdapter(device);
        comm.setBluetoothAdapter(adapter);

        SimpleMessageReceiver.MessageHandler onDisconnect = new SimpleMessageReceiver.MessageHandler() {
            @Override
            public void onMessage(Intent message) {
                assertEquals(BLEComm.ACTION_DISCONNECTED, message.getAction());
                assertTrue(message.hasExtra(BLEComm.EXTRA_DISCONNECT_IS_INTENTIONAL));

                boolean isIntentional = message.getBooleanExtra(BLEComm.EXTRA_DISCONNECT_IS_INTENTIONAL, false);
                assertFalse(isIntentional);
            }
        };

        mReceiver.registerHandler(BLEComm.ACTION_DISCONNECTED, onDisconnect);

        comm.connect("device_address");
        device.sendDisconnected();
    }

    @Test
    public void disconnect_changes_connection_state() {
        final MockDevice device = new MockDevice();
        final MockAdapter adapter = new MockAdapter(device);
        comm.setBluetoothAdapter(adapter);

        comm.connect("device_address");
        device.sendConnected();
        device.sendDisconnected();

        assertEquals(BLEComm.STATE_DISCONNECTED, comm.getConnectionState());
    }

    @Test
    public void startScan_disconnects_before_scanning() {
        final MockDevice device = new MockDevice();
        final MockAdapter adapter = new MockAdapter(device);
        comm.setBluetoothAdapter(adapter);

        comm.connect("device_address");
        device.sendConnected();

        comm.startScan(new BluetoothAdapter.LeScanCallback() {
            @Override
            public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
                assertEquals(BLEComm.STATE_DISCONNECTED, comm.getConnectionState());
            }
        });
    }
}