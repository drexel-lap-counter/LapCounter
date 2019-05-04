package edu.drexel.lapcounter.lapcounter.backend.ble;

import android.content.Context;
import android.content.Intent;
import android.util.CustomAssertions;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import edu.drexel.lapcounter.lapcounter.backend.SimpleMessageReceiver;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.after;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class RSSIManagerTest {
    private static final String TAG = RSSIManagerTest.class.getSimpleName();

    private SimpleMessageReceiver mReceiver;
    private IBroadcastManager mBroadcastManager;
    private BLEComm mComm;
    private RSSIManager mRssiManager;

    @Before
    public void setup() {
        final Context context = mock(Context.class);
        mReceiver = new SimpleMessageReceiver();

        mBroadcastManager = new IBroadcastManager() {
            @Override
            public void sendBroadcast(Intent intent) {
                mReceiver.onReceive(context, intent);
            }
        };

        mComm = mock(BLEComm.class);
        when(mComm.getConnectionState()).thenReturn(BLEComm.STATE_CONNECTED);

        mRssiManager = new RSSIManager(mBroadcastManager, mComm);
        mRssiManager.initCallbacks(mReceiver);
    }

    @After
    public void teardown() {
        mRssiManager.stopRssiRequests();

        mReceiver = null;
        mBroadcastManager = null;
        mComm = null;
        mRssiManager = null;
    }

    private void scheduleRssiRequest() {
        mRssiManager.scheduleRssiRequest();
        Robolectric.flushForegroundThreadScheduler();
    }

    @Test
    public void scheduleRssiRequest_does_not_schedule_when_disconnected() {
        final int STATE_DISCONNECTED = BLEComm.STATE_CONNECTED + 1;
        when(mComm.getConnectionState()).thenReturn(STATE_DISCONNECTED);

        scheduleRssiRequest();

        // Wait at most two times the RSSI polling frequency before asserting that
        // another RSSI request was not scheduled.
        verify(mComm, after(2 * mRssiManager.getPollFrequencyMs()).never()).requestRssi();
    }

    private void setupToSend(final int rssi) {
        // When RSSIManager calls BLEComm::requestRssi(), invoke this callback instead.
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) {
                // Construct an Intent containing an arbitrary RSSI value to send to the
                // RSSIManager.
                Intent rawRssiMsg = new Intent(BLEComm.ACTION_RAW_RSSI_AVAILABLE);
                rawRssiMsg.putExtra(BLEComm.EXTRA_RAW_RSSI, rssi);

                mBroadcastManager.sendBroadcast(rawRssiMsg);
                return null;
            }
        }).when(mComm).requestRssi();
    }

    private void waitBeforeAssert(long delayMs) {
        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException e) {
            fail("Test interrupted");
        }
    }

    @Test
    public void scheduleRssiRequest_schedule_when_connected() {
        // We're eventually going to check that our RSSIManager successfully received this value.
        final int rssiToSend = 72;

        setupToSend(rssiToSend);

        // Kick off RSSI scheduling.
        scheduleRssiRequest();

        waitBeforeAssert(2 * mRssiManager.getPollFrequencyMs());

        // Assert that RSSIManager eventually calls BLEComm::requestRssi
        verify(mComm, atLeastOnce()).requestRssi();

        // And more importantly, assert that the RSSIManager successfully received our raw RSSI
        // value.
        CustomAssertions.assertEquals(mRssiManager.getRssi(), rssiToSend);
    }

    @Test
    public void onRawRssi_receive_null_rssi_val() {
        setupToSend(0);
        scheduleRssiRequest();
        CustomAssertions.assertEquals(mRssiManager.getRssi(), 0);
    }

    @Test
    public void publishRssiAndDirection_when_windows_are_full() {
        final int n = Math.max(RSSIManager.DEFAULT_DELTAS_WINDOW_SIZE,
                RSSIManager.DEFAULT_MOVING_AVERAGE_SIZE);

        SimpleMessageReceiver.MessageHandler onRssiDir = new SimpleMessageReceiver.MessageHandler() {
            @Override
            public void onMessage(Intent message) {
                int direction = message.getIntExtra(RSSIManager.EXTRA_DIRECTION, 0);
                assertEquals(1, direction);

                double rssi = message.getDoubleExtra(RSSIManager.EXTRA_RSSI, 0);
                double expectedRssi = 5 * (n + 1);
                CustomAssertions.assertEquals(expectedRssi, rssi);
            }
        };

        mReceiver.registerHandler(RSSIManager.ACTION_RSSI_AND_DIR_AVAILABLE, onRssiDir);

        for (int i = 1; i <= n; ++i) {
            mRssiManager.stopRssiRequests();
            setupToSend(10 * i);
            scheduleRssiRequest();
        }
    }

    @Test
    public void initCallbacks() {
    }

    @Test
    public void getRssi() {
    }

    @Test
    public void getDirection() {
    }

    @Test
    public void clear() {
    }

    @Test
    public void stopRssiRequests() {
    }

    @Test
    public void setDeltasSize() {
    }

    @Test
    public void setFilterSize() {
    }
}