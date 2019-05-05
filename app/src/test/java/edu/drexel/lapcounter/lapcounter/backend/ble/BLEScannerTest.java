package edu.drexel.lapcounter.lapcounter.backend.ble;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class BLEScannerTest {

    private BLEScanner scanner;

    @Mock
    Context context;

    @Before
    public void setUp() {
        initMocks(this);
        scanner = new BLEScanner(context);
    }

    @Test
    public void setCallback_creates_mLeScanCallback() {
        DeviceScanner.Callback callback = new DeviceScanner.Callback() {
            @Override
            public void onDeviceFound(String deviceName, String deviceAddress, int rssi) {
            }
        };

        scanner.setCallback(callback);

        assertNotNull(scanner.getmLeScanCallback());
    }

    @Test
    public void setAddressWhitelist_sets_whiteList() {
        List<String> strings = new ArrayList<>();
        strings.add("foo");
        strings.add("bar");

        scanner.setAddressWhitelist(strings);

        assertTrue(scanner.getmWhitelist().containsAll(strings));
    }

    @Test
    public void startScan_sets_isscanning_true() {
        when(context.bindService(any(Intent.class), any(ServiceConnection.class), anyInt())).thenReturn(true);

        scanner.startScan();

        assertTrue(scanner.mIsScanning);
    }

    @Test
    public void stopScan_sets_isscanning_false() {
        doNothing().when(context).unbindService(scanner.getmBleServiceConnection());
        BLEService bleService = mock(BLEService.class);
        doNothing().when(bleService).stopScan(any(BluetoothAdapter.LeScanCallback.class));
        scanner.setmBleService(bleService);
        scanner.mIsScanning = true;

        scanner.stopScan();

        assertFalse(scanner.mIsScanning);
    }

    @Test
    public void stopScan_retains_isscanning_false() {
        doNothing().when(context).unbindService(scanner.getmBleServiceConnection());
        BLEService bleService = mock(BLEService.class);
        doNothing().when(bleService).stopScan(any(BluetoothAdapter.LeScanCallback.class));
        scanner.setmBleService(bleService);
        scanner.mIsScanning = false;

        scanner.stopScan();

        assertFalse(scanner.mIsScanning);
    }
}