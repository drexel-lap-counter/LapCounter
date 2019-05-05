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
import static org.junit.Assert.assertFalse;
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
    private SimpleMessageReceiver mReceiver;
    private IBroadcastManager mBroadcastManager;
    private BLEComm mComm;
    private RSSIManager mRssiManager;

    private final static int MAX_WINDOW_SIZE = Math.max(RSSIManager.DEFAULT_DELTAS_WINDOW_SIZE,
            RSSIManager.DEFAULT_MOVING_AVERAGE_SIZE);

    @Before
    public void setup() {
        final Context context = mock(Context.class);
        mReceiver = new SimpleMessageReceiver();

        mBroadcastManager = new IBroadcastManager() {
            @Override
            public void sendBroadcast(Intent intent) {
                // Feed the broadcast intent directly to SimpleMessageReceiver.
                mReceiver.onReceive(context, intent);
            }
        };

        mComm = mock(BLEComm.class);
        setConnectionState(BLEComm.STATE_CONNECTED);

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

    // Begin helper methods
    private void setConnectionState(int state) {
        when(mComm.getConnectionState()).thenReturn(state);
    }

    private void scheduleRssiRequest() {
        mRssiManager.scheduleRssiRequest();
        Robolectric.flushForegroundThreadScheduler();
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

    private void sendRssi(int rssi) {
        setupToSend(rssi);
        scheduleRssiRequest();
    }

    private void waitBeforeAssert(long delayMs) {
        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException e) {
            fail("Test interrupted");
        }
    }


    private void sendNRssiValues(int n) {
        for (int i = 1; i <= n; ++i) {
            mRssiManager.stopRssiRequests();
            sendRssi(10 * i);
        }
    }

    private void fillSlidingWindows() {
        sendNRssiValues(MAX_WINDOW_SIZE);
    }

    private void connect(boolean isReconnect) {
        String action = isReconnect ? BLEComm.ACTION_RECONNECTED : BLEComm.ACTION_CONNECTED;
        mBroadcastManager.sendBroadcast(new Intent(action));
    }

    private void connect() {
        connect(false);
    }

    private void disconnect() {
        mBroadcastManager.sendBroadcast(new Intent(BLEComm.ACTION_DISCONNECTED));
    }

    // Begin unit tests

    @Test
    public void scheduleRssiRequest_does_not_schedule_when_disconnected() {
        final int STATE_DISCONNECTED = BLEComm.STATE_CONNECTED + 1;
        setConnectionState(STATE_DISCONNECTED);

        scheduleRssiRequest();

        // Wait at most two times the RSSI polling frequency before asserting that
        // another RSSI request was not scheduled.
        verify(mComm, after(2 * mRssiManager.getPollFrequencyMs()).never()).requestRssi();
    }

    @Test
    public void scheduleRssiRequest_schedule_when_connected() {
        // We're eventually going to check that our RSSIManager successfully received this value.
        final int rssiToSend = 72;

        sendRssi(rssiToSend);

        waitBeforeAssert(2 * mRssiManager.getPollFrequencyMs());

        // Assert that RSSIManager eventually calls BLEComm::requestRssi
        verify(mComm, atLeastOnce()).requestRssi();

        // And more importantly, assert that the RSSIManager successfully received our raw RSSI
        // value.
        CustomAssertions.assertEquals(mRssiManager.getRssi(), rssiToSend);
    }

    @Test
    public void onRawRssi_receive_null_rssi_val() {
        sendRssi(0);
        CustomAssertions.assertEquals(mRssiManager.getRssi(), 0);
    }


    @Test
    public void publishRssiAndDirection_when_windows_are_full() {
        SimpleMessageReceiver.MessageHandler onRssiDir = new SimpleMessageReceiver.MessageHandler() {
            @Override
            public void onMessage(Intent message) {
                int direction = message.getIntExtra(RSSIManager.EXTRA_DIRECTION, 0);
                assertEquals(1, direction);

                double rssi = message.getDoubleExtra(RSSIManager.EXTRA_RSSI, 0);
                double expectedRssi = 5 * (MAX_WINDOW_SIZE + 1);
                CustomAssertions.assertEquals(expectedRssi, rssi);
            }
        };

        mReceiver.registerHandler(RSSIManager.ACTION_RSSI_AND_DIR_AVAILABLE, onRssiDir);

        fillSlidingWindows();
    }

    @Test
    public void publishRssiAndDirection_changes_to_slower_poll_freq_when_full() {
        final int prevFreq = mRssiManager.getPollFrequencyMs();

        SimpleMessageReceiver.MessageHandler onRssiDir = new SimpleMessageReceiver.MessageHandler() {
            @Override
            public void onMessage(Intent message) {
                assertTrue(mRssiManager.getPollFrequencyMs() > prevFreq);
            }
        };

        mReceiver.registerHandler(RSSIManager.ACTION_RSSI_AND_DIR_AVAILABLE, onRssiDir);

        fillSlidingWindows();
    }

    @Test
    public void publishRssiAndDirection_does_not_publish_when_not_full() {
        SimpleMessageReceiver.MessageHandler onRssiDir = new SimpleMessageReceiver.MessageHandler() {
            @Override
            public void onMessage(Intent message) {
                fail("Wasn't supposed to receive RSSI and direction.");
            }
        };

        mReceiver.registerHandler(RSSIManager.ACTION_RSSI_AND_DIR_AVAILABLE, onRssiDir);

        sendNRssiValues(MAX_WINDOW_SIZE / 2);

        waitBeforeAssert(2 * mRssiManager.getPollFrequencyMs());
    }

    @Test
    public void connect_changes_rssi_poll_frequency() {
        // We'll first need to induce the RSSIManager to change its poll frequency to
        // a different value by filling its windows.

        SimpleMessageReceiver.MessageHandler onRssiDir = new SimpleMessageReceiver.MessageHandler() {
            @Override
            public void onMessage(Intent message) {
                mRssiManager.stopRssiRequests();

                // So the poll frequency has changed.
                int prevFreq = mRssiManager.getPollFrequencyMs();

                // Now let's simulate a connect.
                connect();

                // Poll frequency should have changed to a lower value.
                assertTrue(mRssiManager.getPollFrequencyMs() < prevFreq);
            }
        };

        mReceiver.registerHandler(RSSIManager.ACTION_RSSI_AND_DIR_AVAILABLE, onRssiDir);

        fillSlidingWindows();
    }

    @Test
    public void reconnect_changes_rssi_poll_frequency() {
        // We'll first need to induce the RSSIManager to change its poll frequency to
        // a different value by filling its windows.
        SimpleMessageReceiver.MessageHandler onRssiDir = new SimpleMessageReceiver.MessageHandler() {
            @Override
            public void onMessage(Intent message) {
                mRssiManager.stopRssiRequests();

                // So the poll frequency has changed.
                int prevFreq = mRssiManager.getPollFrequencyMs();

                // Now let's simulate a reconnect.
                connect(true);

                // Poll frequency should have changed to a lower value.
                assertTrue(mRssiManager.getPollFrequencyMs() < prevFreq);
            }
        };

        mReceiver.registerHandler(RSSIManager.ACTION_RSSI_AND_DIR_AVAILABLE, onRssiDir);

        fillSlidingWindows();
    }


    @Test
    public void connect_first_time_doesnt_schedule_rssi_requests() {
        connect();
        Robolectric.flushForegroundThreadScheduler();
        verify(mComm, after(2 * mRssiManager.getPollFrequencyMs()).never()).requestRssi();
    }

    @Test
    public void reconnect_schedules_rssi_requests() {
        // Simulate already having scheduled RSSI requests.
        sendNRssiValues(1);

        disconnect();

        // Successful reconnection.
        connect(true);

        // Execute postponed Handler tasks.
        Robolectric.flushForegroundThreadScheduler();

        verify(mComm, after(2 * mRssiManager.getPollFrequencyMs()).times(2)).requestRssi();
    }

    @Test
    public void clear_resets_sliding_windows() {
        SimpleMessageReceiver.MessageHandler onRssiDir = new SimpleMessageReceiver.MessageHandler() {
            @Override
            public void onMessage(Intent message) {
                mRssiManager.stopRssiRequests();

                assertTrue(mRssiManager.windowsAreFull());
                mRssiManager.clear();
                assertFalse(mRssiManager.windowsAreFull());
            }
        };

        mReceiver.registerHandler(RSSIManager.ACTION_RSSI_AND_DIR_AVAILABLE, onRssiDir);

        fillSlidingWindows();
    }

    @Test
    public void clear_allows_items_to_be_added_afterwards() {
        SimpleMessageReceiver.MessageHandler onRssiDir = new SimpleMessageReceiver.MessageHandler() {
            private int mHitCount = 0;

            @Override
            public void onMessage(Intent message) {
                mRssiManager.stopRssiRequests();

                ++mHitCount;

                if (mHitCount == 2) {
                    // We successfully filled the sliding windows a second time
                    // after calling clear().
                    assertTrue(mRssiManager.windowsAreFull());
                    return;
                }

                mRssiManager.clear();

                // Try to fill sliding windows again.
                fillSlidingWindows();
            }
        };

        mReceiver.registerHandler(RSSIManager.ACTION_RSSI_AND_DIR_AVAILABLE, onRssiDir);

        fillSlidingWindows();
    }

    @Test
    public void setFilterSize_size_zero_sends_rssi_as_zero() {
        SimpleMessageReceiver.MessageHandler onRssiDir = new SimpleMessageReceiver.MessageHandler() {

            @Override
            public void onMessage(Intent message) {
                double rssi = message.getDoubleExtra(RSSIManager.EXTRA_RSSI, -1);
                CustomAssertions.assertEquals(rssi, 0);
            }
        };

        mReceiver.registerHandler(RSSIManager.ACTION_RSSI_AND_DIR_AVAILABLE, onRssiDir);

        mRssiManager.setFilterSize(0);
        fillSlidingWindows();
    }

    @Test
    public void setFilterSize_size_one_sends_raw_rssi() {
        final int n = Math.max(1, RSSIManager.DEFAULT_DELTAS_WINDOW_SIZE);
        final int lastRawRssiSent = 10 * n;

        SimpleMessageReceiver.MessageHandler onRssiDir = new SimpleMessageReceiver.MessageHandler() {

            @Override
            public void onMessage(Intent message) {
                double rssi = message.getDoubleExtra(RSSIManager.EXTRA_RSSI, -1);
                CustomAssertions.assertEquals(rssi, lastRawRssiSent);
            }
        };

        mReceiver.registerHandler(RSSIManager.ACTION_RSSI_AND_DIR_AVAILABLE, onRssiDir);

        mRssiManager.setFilterSize(1);
        sendNRssiValues(n);
    }

    @Test
    public void setFilterSize_size_n_publishes_rssi_and_dir() {
        final int newFilterSize = 2 * RSSIManager.DEFAULT_MOVING_AVERAGE_SIZE;
        final int n = Math.max(newFilterSize, RSSIManager.DEFAULT_DELTAS_WINDOW_SIZE);

        SimpleMessageReceiver.MessageHandler onRssiDir = new SimpleMessageReceiver.MessageHandler() {

            @Override
            public void onMessage(Intent message) {
                double rssi = message.getDoubleExtra(RSSIManager.EXTRA_RSSI, -1);
                CustomAssertions.assertEquals(rssi, 5 * (n + 1));
            }
        };

        mReceiver.registerHandler(RSSIManager.ACTION_RSSI_AND_DIR_AVAILABLE, onRssiDir);

        mRssiManager.setFilterSize(newFilterSize);
        sendNRssiValues(n);
    }

    @Test
    public void setDeltas_size_zero_yields_zero_direction() {
        mRssiManager.setDeltasSize(0);
        sendNRssiValues(1);
        assertEquals(0, mRssiManager.getDirection());
    }

    @Test
    public void setDeltas_size_one_yields_sign_of_last_delta() {
        // Use raw rssi values
        mRssiManager.setFilterSize(1);

        final int n = 2;

        mRssiManager.setDeltasSize(1);
        sendNRssiValues(n);

        // Positive direction
        assertEquals(1, mRssiManager.getDirection());
    }

    @Test
    public void getDirection_empty_deltas_zero_direction() {
        assertEquals(0, mRssiManager.getDirection());
    }

    @Test
    public void getDirection_positive_direction() {
        sendNRssiValues(2);
        assertEquals(1, mRssiManager.getDirection());
    }

    @Test
    public void getDirection_negative_direction() {
        sendNRssiValues(2);

        sendRssi(15);
        sendRssi(10);
        sendRssi(5);
        sendRssi(1);

        assertEquals(-1, mRssiManager.getDirection());
    }
}