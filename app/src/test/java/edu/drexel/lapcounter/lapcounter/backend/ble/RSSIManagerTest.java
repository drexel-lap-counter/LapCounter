package edu.drexel.lapcounter.lapcounter.backend.ble;

import android.content.Context;
import android.content.Intent;
import android.util.CustomAssertions;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import edu.drexel.lapcounter.lapcounter.backend.SimpleMessageReceiver;

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

    private IBroadcastManager mBroadcastManager;
    private BLEComm mComm;
    private RSSIManager mRssiManager;

    @Before
    public void setup() {
        final Context context = mock(Context.class);
        final SimpleMessageReceiver receiver = new SimpleMessageReceiver();

        mBroadcastManager = new IBroadcastManager() {
            @Override
            public void sendBroadcast(Intent intent) {
                receiver.onReceive(context, intent);
            }
        };

        mComm = mock(BLEComm.class);
        mRssiManager = new RSSIManager(mBroadcastManager, mComm);
        mRssiManager.initCallbacks(receiver);
    }

    @Test
    public void scheduleRssiRequest_does_not_schedule_when_disconnected() {
        final int STATE_DISCONNECTED = BLEComm.STATE_CONNECTED + 1;
        when(mComm.getConnectionState()).thenReturn(STATE_DISCONNECTED);

        mRssiManager.scheduleRssiRequest();

        // Run Handler::postDelayed() callbacks if needed.
        Robolectric.flushForegroundThreadScheduler();

        // Wait at most two times the RSSI polling frequency before asserting that
        // another RSSI request was not scheduled.
        verify(mComm, after(2 * mRssiManager.getPollFrequencyMs()).never()).requestRssi();
    }

    @Test
    public void scheduleRssiRequest_schedule_when_connected() {
        // Fake a successful connection state.
        when(mComm.getConnectionState()).thenReturn(BLEComm.STATE_CONNECTED);

        // We're eventually going to check that our RSSIManager successfully received this value.
        final int rssiToSend = 72;

        // When RSSIManager calls BLEComm::requestRssi(), invoke this callback instead.
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) {
                // Construct an Intent containing an arbitrary RSSI value to send to the
                // RSSIManager.
                Intent rawRssiMsg = new Intent(BLEComm.ACTION_RAW_RSSI_AVAILABLE);
                rawRssiMsg.putExtra(BLEComm.EXTRA_RAW_RSSI, rssiToSend);

                mBroadcastManager.sendBroadcast(rawRssiMsg);
                return null;
            }
        }).when(mComm).requestRssi();

        // Kick off RSSI scheduling.
        mRssiManager.scheduleRssiRequest();

        // Run Handler::postDelayed() callbacks if needed.
        Robolectric.flushForegroundThreadScheduler();

        final long timeToWaitBeforeAssertMs = 2 * mRssiManager.getPollFrequencyMs();

        try {
            Thread.sleep(timeToWaitBeforeAssertMs);
        } catch (InterruptedException e) {
            fail("Test interrupted");
        }

        // Assert that RSSIManager eventually calls BLEComm::requestRssi
        verify(mComm, atLeastOnce()).requestRssi();

        // And more importantly, assert that the RSSIManager successfully received our raw RSSI
        // value.
        CustomAssertions.assertEquals(mRssiManager.getRssi(), rssiToSend);
    }

    @Test
    public void publishRssiAndDirection_when_windows_are_full() {

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