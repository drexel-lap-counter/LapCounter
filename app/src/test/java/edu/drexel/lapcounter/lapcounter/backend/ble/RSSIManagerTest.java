package edu.drexel.lapcounter.lapcounter.backend.ble;

import android.content.Context;
import android.content.Intent;
import android.util.CustomAssertions;
import android.util.Log;

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

    private BLEComm mComm;
    private RSSIManager mManager;

    @Before
    public void setup() {
        IBroadcastManager mockManager = new IBroadcastManager() {
            @Override
            public void sendBroadcast(Intent intent) {
                Log.i(TAG, "Mock sendBroadcast()");
            }
        };

        mComm = mock(BLEComm.class);
        mManager = new RSSIManager(mockManager, mComm);
    }

    @Test
    public void scheduleRssiRequest_does_not_schedule_when_disconnected() {
        final int STATE_DISCONNECTED = BLEComm.STATE_CONNECTED + 1;
        when(mComm.getConnectionState()).thenReturn(STATE_DISCONNECTED);

        mManager.scheduleRssiRequest();

        // Run Handler::postDelayed() callbacks if needed.
        Robolectric.flushForegroundThreadScheduler();

        // Wait at most two times the RSSI polling frequency before asserting that
        // another RSSI request was not scheduled.
        verify(mComm, after(2 * mManager.getPollFrequencyMs()).never()).requestRssi();
    }

    @Test
    public void scheduleRssiRequest_schedule_when_connected() {
        // Fake a successful connection state.
        when(mComm.getConnectionState()).thenReturn(BLEComm.STATE_CONNECTED);

        // Let's register a SimpleMessageReceiver that we directly invoke with Intents.
        final SimpleMessageReceiver messageReceiver = new SimpleMessageReceiver();
        mManager.initCallbacks(messageReceiver);

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

                // Directly invoke SimpleMessageReceiver::onReceive() to call RSSIManager's
                // code that processes incoming raw RSSI values.
                messageReceiver.onReceive(mock(Context.class), rawRssiMsg);
                return null;
            }
        }).when(mComm).requestRssi();

        // Kick off RSSI scheduling.
        mManager.scheduleRssiRequest();

        // Run Handler::postDelayed() callbacks if needed.
        Robolectric.flushForegroundThreadScheduler();

        final long timeToWaitBeforeAssertMs = 2 * mManager.getPollFrequencyMs();

        try {
            Thread.sleep(timeToWaitBeforeAssertMs);
        } catch (InterruptedException e) {
            fail("Test interrupted");
        }

        // Assert that RSSIManager eventually calls BLEComm::requestRssi
        verify(mComm, atLeastOnce()).requestRssi();

        // And more importantly, assert that the RSSIManager successfully received our raw RSSI
        // value.
        CustomAssertions.assertEquals(mManager.getRssi(), rssiToSend);
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