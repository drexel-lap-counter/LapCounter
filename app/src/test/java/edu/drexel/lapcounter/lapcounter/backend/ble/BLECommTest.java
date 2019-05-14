package edu.drexel.lapcounter.lapcounter.backend.ble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.content.Context;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.*;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public class BLECommTest {

    BLEComm comm;
    @Mock
    Context context;
    @Mock
    BluetoothAdapter adapter;
    @Mock BluetoothDevice device;
    @Mock
    BluetoothGatt gatt;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        comm = new BLEComm(context, adapter);
    }

    @Test
    public void requestRssi_calls_gatt_if_connected() {
        comm.setmConnectionState(comm.STATE_CONNECTED);
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
        comm.setmBluetoothAdapter(null);
        String address = "some address";

        boolean connect = comm.connect(address);

        assertEquals(false, connect);
    }

    @Test
    public void connect_returns_false_if_address_is_null() {
        String address = null;

        boolean connect = comm.connect(address);

        assertEquals(false, connect);
    }

    @Test
    public void connect_returns_true_if_already_connecting() {
        comm.setmConnectionState(BLEComm.STATE_CONNECTING);
        String address = "some address";

        boolean connect = comm.connect(address);

        assertEquals(true, connect);
    }

    @Test
    public void connect_returns_false_if_device_is_null() {
        String address = "some address";
        when(adapter.getRemoteDevice(address)).thenReturn(null);
        comm.setmBluetoothAdapter(adapter);

        boolean connect = comm.connect(address);

        assertEquals(false, connect);
    }

    @Test
    public void connect_returns_true_if_device_is_not_null() {
        String address = "some address";
        when(adapter.getRemoteDevice(address)).thenReturn(device);
        comm.setmBluetoothAdapter(adapter);

        boolean connect = comm.connect(address);

        assertEquals(true, connect);
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

        comm.startScan(callback);

        verify(adapter, only()).startLeScan(callback);
    }

    @Test
    public void stopScan_calls_adapter_stopscan() {
        BluetoothAdapter.LeScanCallback callback = mock(BluetoothAdapter.LeScanCallback.class);

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
}