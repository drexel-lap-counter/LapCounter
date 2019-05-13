package edu.drexel.lapcounter.lapcounter.backend.ble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.util.CustomAssertions;

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BLEScannerTest {
    private BLEScanner scanner;

    private BLEService mBleService;

    @Before
    public void setUp() {
        mBleService = mock(BLEService.class);
        BLEService.LocalBinder mockBinder = mock(BLEService.LocalBinder.class);

        when(mockBinder.getService()).thenReturn(mBleService);
        when(mBleService.onBind(any(Intent.class))).thenReturn(mockBinder);

        doNothing().when(mBleService).startScan(any(BluetoothAdapter.LeScanCallback.class));
        doNothing().when(mBleService).stopScan(any(BluetoothAdapter.LeScanCallback.class));

        IContext context = new IContext() {
            @Override
            public boolean bindService(Intent service, ServiceConnection connection, int flags) {
                connection.onServiceConnected(null, mBleService.onBind(service));
                return true;
            }

            @Override
            public void unbindService(ServiceConnection connection) {
                connection.onServiceDisconnected(null);
            }

            @Override
            public Context getInner() {
                return null;
            }
        };

        scanner = new BLEScanner(context);
    }

    @Test
    public void setCallback_creates_mLeScanCallback() {
        DeviceScanner.Callback callback = new DeviceScanner.Callback() {
            @Override
            public void onDeviceFound(String deviceName, String deviceAddress, int rssi) {
            }
        };

        doNothing().when(mBleService).startScan(any(BluetoothAdapter.LeScanCallback.class));

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
        scanner.startScan();
        assertTrue(scanner.mIsScanning);
    }

    @Test
    public void stopScan_sets_isscanning_false() {
        scanner.startScan();
        scanner.stopScan();

        assertFalse(scanner.mIsScanning);
    }

    @Test
    public void stopScan_retains_isscanning_false() {
        scanner.mIsScanning = false;

        scanner.stopScan();

        assertFalse(scanner.mIsScanning);
    }

    class VerifyCallback implements DeviceScanner.Callback {
        private final List<String> mWhitelist;
        private int mNumExpectedDevicesLeft;

        VerifyCallback(List<String> whitelist) {
            mWhitelist = whitelist;
            mNumExpectedDevicesLeft = mWhitelist.size();
        }

        @Override
        public void onDeviceFound(String deviceName, String deviceAddress, int rssi) {
            // Verify that we get devices on the whitelist.

            assertTrue(mWhitelist.contains(deviceAddress));

            --mNumExpectedDevicesLeft;

            if (mNumExpectedDevicesLeft < 0) {
                fail(String.format("Expected exactly %d whitelisted devices.", mWhitelist.size()));
            }
        }
    }


    @Test
    public void scan_items_on_whitelist_show_up() {
        final List<String> whitelistedDevices = new ArrayList<>();
        whitelistedDevices.add("foo");
        whitelistedDevices.add("bar");
        whitelistedDevices.add("baz");

        scanner.setAddressWhitelist(whitelistedDevices);

        VerifyCallback scanCallback = new VerifyCallback(whitelistedDevices);

        // Simulate BLEService finding devices and sending them to our callback.
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) {
                for (String address : whitelistedDevices) {
                    BluetoothDevice mockDevice = mock(BluetoothDevice.class);
                    when(mockDevice.getName()).thenReturn("");
                    when(mockDevice.getAddress()).thenReturn(address);
                    scanner.getmLeScanCallback().onLeScan(mockDevice, 72, null);
                }
                return null;
            }
        }).when(mBleService).startScan(any(BluetoothAdapter.LeScanCallback.class));

        scanner.setCallback(scanCallback);
        scanner.startScan();

        CustomAssertions.waitBeforeAssert(1000);

        if (scanCallback.mNumExpectedDevicesLeft > 0) {
            fail("Didn't receive all sent whitelisted devices.");
        }
    }

    @Test
    public void scan_items_not_on_whitelist_dont_show_up() {
        final List<String> whitelistedDevices = new ArrayList<>();
        whitelistedDevices.add("foo");
        whitelistedDevices.add("bar");
        whitelistedDevices.add("baz");

        final List<String> allDevices = new ArrayList<>(whitelistedDevices);
        allDevices.add("not");
        allDevices.add("whitelisted");
        allDevices.add("so");
        allDevices.add("don't");
        allDevices.add("show");
        allDevices.add("up");

        scanner.setAddressWhitelist(whitelistedDevices);

        VerifyCallback scanCallback = new VerifyCallback(whitelistedDevices);

        // Simulate BLEService finding devices and sending them to our callback.
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) {
                for (String address : allDevices) {
                    BluetoothDevice mockDevice = mock(BluetoothDevice.class);
                    when(mockDevice.getName()).thenReturn("");
                    when(mockDevice.getAddress()).thenReturn(address);
                    scanner.getmLeScanCallback().onLeScan(mockDevice, 72, null);
                }
                return null;
            }
        }).when(mBleService).startScan(any(BluetoothAdapter.LeScanCallback.class));

        scanner.setCallback(scanCallback);
        scanner.startScan();

        CustomAssertions.waitBeforeAssert(1000);

        if (scanCallback.mNumExpectedDevicesLeft > 0) {
            fail("Didn't receive all sent whitelisted devices.");
        }
    }
}