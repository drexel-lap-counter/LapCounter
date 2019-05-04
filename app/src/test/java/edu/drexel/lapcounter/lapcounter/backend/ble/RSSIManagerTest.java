package edu.drexel.lapcounter.lapcounter.backend.ble;

import android.content.Context;
import android.content.Intent;
import android.util.CustomAssertions;
import android.util.Log;

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

    @Test
    public void scheduleRssiRequest_does_not_schedule_when_disconnected() {
        IBroadcastManager mockManager = new IBroadcastManager() {
            @Override
            public void sendBroadcast(Intent intent) {
                Log.i(TAG, "Mock sendBroadcast()");
            }
        };

        BLEComm mockComm = mock(BLEComm.class);
        RSSIManager rm = new RSSIManager(mockManager, mockComm);

        final int STATE_DISCONNECTED = BLEComm.STATE_CONNECTED + 1;
        when(mockComm.getConnectionState()).thenReturn(STATE_DISCONNECTED);

        rm.scheduleRssiRequest();

        // Run Handler::postDelayed() callbacks if needed.
        Robolectric.flushForegroundThreadScheduler();

        // Wait at most two times the RSSI polling frequency before asserting that
        // another RSSI request was not scheduled.
        verify(mockComm, after(2 * rm.getPollFrequencyMs()).never()).requestRssi();
    }

    @Test
    public void scheduleRssiRequest_schedule_when_connected() {
        IBroadcastManager mockManager = new IBroadcastManager() {
            @Override
            public void sendBroadcast(Intent intent) {
                Log.i(TAG, "Mock sendBroadcast()");
            }
        };

        BLEComm mockComm = mock(BLEComm.class);
        RSSIManager rm = new RSSIManager(mockManager, mockComm);

        // Fake a successful connection state.
        when(mockComm.getConnectionState()).thenReturn(BLEComm.STATE_CONNECTED);

        // Let's register a SimpleMessageReceiver that we directly invoke with Intents.
        final SimpleMessageReceiver messageReceiver = new SimpleMessageReceiver();
        rm.initCallbacks(messageReceiver);

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
        }).when(mockComm).requestRssi();

        // Kick off RSSI scheduling.
        rm.scheduleRssiRequest();

        // Run Handler::postDelayed() callbacks if needed.
        Robolectric.flushForegroundThreadScheduler();

        final long timeToWaitBeforeAssertMs = 2 * rm.getPollFrequencyMs();

        try {
            Thread.sleep(timeToWaitBeforeAssertMs);
        } catch (InterruptedException e) {
            fail("Test interrupted");
        }

        // Assert that RSSIManager eventually calls BLEComm::requestRssi
        verify(mockComm, atLeastOnce()).requestRssi();

        // And more importantly, assert that the RSSIManager successfully received our raw RSSI
        // value.
        CustomAssertions.assertEquals(rm.getRssi(), rssiToSend);
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