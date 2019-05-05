package edu.drexel.lapcounter.lapcounter.backend.ble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.IBinder;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import edu.drexel.lapcounter.lapcounter.backend.SimpleMessageReceiver;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

// By design, BLEService is a lightweight container that provides a Service interface for a
// BLEComm instance and a RSSIManager instance. Therefore, the unit tests should focus on
// verifying that BLEService's interface forwards to the appropriate BLEComm/RSSIManager instances.
@RunWith(RobolectricTestRunner.class)
public class BLEServiceTest {
    private BLEComm mMockComm;
    private RSSIManager mMockRssiManager;
    private BLEService mBleService;
    private SimpleMessageReceiver mReceiver;

    @Before
    public void setup() {
        mMockComm = mock(BLEComm.class);
        mMockRssiManager = mock(RSSIManager.class);
        mReceiver = mock(SimpleMessageReceiver.class);
        mBleService = new BLEService(mMockComm, mMockRssiManager, mReceiver);
    }

    @Test
    public void bind_service() {
        IBinder binder = mBleService.onBind(null);
        BLEService.LocalBinder bleBinder = (BLEService.LocalBinder) binder;
        assertNotNull(bleBinder);
        assertEquals(mBleService, bleBinder.getService());
    }

    // BLEComm forwards

    @Test
    public void startScan_forwards_to_comm() {
        BluetoothAdapter.LeScanCallback scanCallback = new BluetoothAdapter.LeScanCallback() {
            @Override
            public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            }
        };

        mBleService.startScan(scanCallback);
        verify(mMockComm, times(1)).startScan(scanCallback);
    }

    @Test
    public void stopScan_forwards_to_comm() {
        BluetoothAdapter.LeScanCallback scanCallback = new BluetoothAdapter.LeScanCallback() {
            @Override
            public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            }
        };

        mBleService.stopScan(scanCallback);
        verify(mMockComm, times(1)).stopScan(scanCallback);
    }

    @Test
    public void connectToDevice_forwards_to_comm() {
        String address = "device";
        mBleService.connectToDevice(address);
        verify(mMockComm, times(1)).connect(address);
    }

    @Test
    public void disconnectDevice_forwards_to_comm() {
        String address = "device";
        mBleService.connectToDevice(address);
        mBleService.disconnectDevice();
        verify(mMockComm, times(1)).disconnect();
    }

    // RSSIManager forwards

    @Test
    public void startRssiRequests_forwards_to_manager() {
        mBleService.startRssiRequests();
        verify(mMockRssiManager, times(1)).scheduleRssiRequest();
    }

    @Test
    public void stopRssiRequests_forwards_to_manager() {
        mBleService.startRssiRequests();
        mBleService.stopRssiRequests();
        verify(mMockRssiManager, times(1)).stopRssiRequests();
    }

    @Test
    public void setRssiManagerWindowSizes_forwards_to_manager() {
        final int newDeltasSize = 10;
        final int newFilterSize = 300;

        mBleService.setRssiManagerWindowSizes(newDeltasSize, newFilterSize);

        verify(mMockRssiManager, times(1)).setDeltasSize(newDeltasSize);
        verify(mMockRssiManager, times(1)).setFilterSize(newFilterSize);
    }

    // Activity lifecycle tests

    @Test
    public void onDestroy_disconnects_device() {
        String address = "device";
        mBleService.connectToDevice(address);
        verify(mMockComm, times(1)).connect(address);

        doNothing().when(mReceiver).detach(any(Context.class));

        mBleService.onDestroy();
        verify(mMockComm, times(1)).disconnect();
    }
}