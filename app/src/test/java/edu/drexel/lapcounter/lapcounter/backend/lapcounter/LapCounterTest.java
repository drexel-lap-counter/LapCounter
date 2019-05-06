package edu.drexel.lapcounter.lapcounter.backend.lapcounter;

import android.content.Intent;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import edu.drexel.lapcounter.lapcounter.backend.SimpleMessageReceiver;
import edu.drexel.lapcounter.lapcounter.backend.ble.IBroadcastManager;

import static edu.drexel.lapcounter.lapcounter.backend.lapcounter.LocationStateMachine.State.FAR;
import static edu.drexel.lapcounter.lapcounter.backend.lapcounter.LocationStateMachine.State.NEAR;
import static edu.drexel.lapcounter.lapcounter.backend.lapcounter.LocationStateMachine.State.UNKNOWN;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
public class LapCounterTest {
    private SimpleMessageReceiver mReceiver;
    private IBroadcastManager mBroadcastManager;
    private LapCounter mLapCounter;

    @Before
    public void setup() {
        mReceiver = new SimpleMessageReceiver();

        mBroadcastManager = new IBroadcastManager() {
            @Override
            public void sendBroadcast(Intent intent) {
                // Feed the broadcast intent directly to SimpleMessageReceiver.
                mReceiver.onReceive(null, intent);
            }
        };

        mLapCounter = new LapCounter(mBroadcastManager);
        mLapCounter.initCallbacks(mReceiver);
    }

    private void sendTransition(LocationStateMachine.State before,
                                LocationStateMachine.State after) {
        Intent intent = new Intent(LocationStateMachine.ACTION_STATE_TRANSITION);
        intent.putExtra(LocationStateMachine.EXTRA_STATE_BEFORE, before);
        intent.putExtra(LocationStateMachine.EXTRA_STATE_AFTER, after);
        mBroadcastManager.sendBroadcast(intent);
    }

    class MH implements SimpleMessageReceiver.MessageHandler
    {
        private int lapCount = 0;

        @Override
        public void onMessage(Intent message) {
            int lapCount = message.getIntExtra(LapCounter.EXTRA_LAP_COUNT, -1);

            if (lapCount == -1) {
                fail("Lap count message didn't contain the current lap count as an extra.");
            }

            this.lapCount = lapCount;
        }
    }

    private MH setupHandler() {
        MH onLapCount = new MH();
        mReceiver.registerHandler(LapCounter.ACTION_LAP_COUNT_UPDATED, onLapCount);
        return onLapCount;
    }

    private void assertLapCount(int count, LocationStateMachine.State before,
                                LocationStateMachine.State after) {
        MH onLapCount = setupHandler();
        assertEquals(Math.max(0, count - 2), onLapCount.lapCount);
        sendTransition(before, after);
        assertEquals(count, onLapCount.lapCount);
    }

    @Test
    public void publishLaps_far_to_near() {
        assertLapCount(2, FAR, NEAR);
    }

    @Test
    public void publishLaps_near_to_far() {
        assertLapCount(0, NEAR, FAR);
    }

    @Test
    public void publishLaps_near_to_near() {
        assertLapCount(0, NEAR, NEAR);
    }

    @Test
    public void publishLaps_far_to_far() {
        assertLapCount(0, FAR, FAR);
    }

    @Test
    public void publishLaps_near_to_unknown() {
        assertLapCount(0, NEAR, UNKNOWN);
    }

    @Test
    public void publishLaps_far_to_unknown() {
        assertLapCount(0, FAR, UNKNOWN);
    }

    @Test
    public void publishLaps_unknown_to_near() {
        assertLapCount(0, UNKNOWN, NEAR);
    }

    @Test
    public void publishLaps_unknown_to_far() {
        assertLapCount(0, UNKNOWN, FAR);
    }

    @Test
    public void publishLaps_unknown_to_unknown() {
        assertLapCount(0, UNKNOWN, UNKNOWN);
    }

    @Test
    public void publishLaps_consecutive() {
        MH onLapCount = setupHandler();

        assertEquals(0, onLapCount.lapCount);

        sendTransition(FAR, NEAR);
        assertEquals(2, onLapCount.lapCount);

        sendTransition(NEAR, FAR);
        assertEquals(2, onLapCount.lapCount);

        sendTransition(FAR, NEAR);
        assertEquals(4, onLapCount.lapCount);
    }

    @Test
    public void onMissedLaps_increments_lap_count() {
        MH onLapCount = setupHandler();

        assertEquals(0, onLapCount.lapCount);

        sendTransition(FAR, NEAR);
        assertEquals(2, onLapCount.lapCount);

        sendTransition(NEAR, FAR);
        assertEquals(2, onLapCount.lapCount);

        sendTransition(FAR, NEAR);
        assertEquals(4, onLapCount.lapCount);

        mBroadcastManager.sendBroadcast(new Intent(DisconnectManager.ACTION_MISSED_LAPS));
        assertEquals(6, onLapCount.lapCount);
    }
}