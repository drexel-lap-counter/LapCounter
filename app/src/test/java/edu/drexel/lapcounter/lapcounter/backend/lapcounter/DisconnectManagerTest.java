package edu.drexel.lapcounter.lapcounter.backend.lapcounter;

import android.content.Intent;
import android.util.CustomAssertions;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import edu.drexel.lapcounter.lapcounter.backend.SimpleMessageReceiver;
import edu.drexel.lapcounter.lapcounter.backend.ble.BLEComm;
import edu.drexel.lapcounter.lapcounter.backend.ble.IBroadcastManager;
import edu.drexel.lapcounter.lapcounter.backend.ble.RSSIManager;

import static edu.drexel.lapcounter.lapcounter.backend.lapcounter.LocationStateMachine.State.FAR;
import static edu.drexel.lapcounter.lapcounter.backend.lapcounter.LocationStateMachine.State.NEAR;
import static edu.drexel.lapcounter.lapcounter.backend.lapcounter.LocationStateMachine.State.UNKNOWN;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class DisconnectManagerTest {

    private SimpleMessageReceiver mReceiver;
    private IBroadcastManager mBroadcastManager;
    private DisconnectManager mDisconnectManager;

    @Before
    public void setup() {
        mReceiver = new SimpleMessageReceiver();

        mBroadcastManager = new IBroadcastManager() {
            @Override
            public void sendBroadcast(Intent intent) {
                mReceiver.onReceive(null, intent);
            }
        };

        mDisconnectManager = new DisconnectManager(mBroadcastManager);
        mDisconnectManager.initCallbacks(mReceiver);
    }

    private void sendRssiAndDir(double rssi, int direction) {
        Intent intent = new Intent(RSSIManager.ACTION_RSSI_AND_DIR_AVAILABLE);
        intent.putExtra(RSSIManager.EXTRA_RSSI, rssi);
        intent.putExtra(RSSIManager.EXTRA_DIRECTION, direction);
        mBroadcastManager.sendBroadcast(intent);
    }

    private void sendTransition(LocationStateMachine.State before,
                                LocationStateMachine.State after) {
        Intent intent = new Intent(LocationStateMachine.ACTION_STATE_TRANSITION);
        intent.putExtra(LocationStateMachine.EXTRA_STATE_BEFORE, before);
        intent.putExtra(LocationStateMachine.EXTRA_STATE_AFTER, after);
        mBroadcastManager.sendBroadcast(intent);
    }


    @Test
    public void onRssi_updates_current_state_distRssi() {
        // First assert that we haven't updated the AthleteState.
        AthleteState currentState = mDisconnectManager.getCurrentState();
        CustomAssertions.assertEquals(0, currentState.distRssi);

        int rssiToSend = 72;
        sendRssiAndDir(rssiToSend, 0);
        CustomAssertions.assertEquals(rssiToSend, currentState.distRssi);

        rssiToSend *= 0.75;
        sendRssiAndDir(rssiToSend, 0);
        CustomAssertions.assertEquals(rssiToSend, currentState.distRssi);
    }

    @Test
    public void onRssi_updates_current_state_travelDir() {
        // First assert that we haven't updated the AthleteState.
        AthleteState currentState = mDisconnectManager.getCurrentState();
        CustomAssertions.assertEquals(0, currentState.travelDirection);

        // Outward direction
        int directionToSend = 1;
        sendRssiAndDir(54, directionToSend);
        CustomAssertions.assertEquals(directionToSend, currentState.travelDirection);

        // Inward
        directionToSend = -1;
        sendRssiAndDir(54, directionToSend);
        CustomAssertions.assertEquals(directionToSend, currentState.travelDirection);
    }

    @Test
    public void onRssi_updates_retains_previous_travelDir_on_zero_direction() {
        // First assert that we haven't updated the AthleteState.
        AthleteState currentState = mDisconnectManager.getCurrentState();
        CustomAssertions.assertEquals(0, currentState.travelDirection);

        // Outward direction
        int directionToSend = 1;
        sendRssiAndDir(54, directionToSend);
        CustomAssertions.assertEquals(directionToSend, currentState.travelDirection);

        // Zero direction
        sendRssiAndDir(54, 0);
        CustomAssertions.assertEquals(directionToSend, currentState.travelDirection);

        // Inward direction
        directionToSend = -1;
        sendRssiAndDir(54, directionToSend);
        CustomAssertions.assertEquals(directionToSend, currentState.travelDirection);


        // Zero direction
        sendRssiAndDir(54, 0);
        CustomAssertions.assertEquals(directionToSend, currentState.travelDirection);
    }

    @Test
    public void onTransition_updates_current_zone() {
        AthleteState state = mDisconnectManager.getCurrentState();
        assertNull(state.zone);

        sendTransition(UNKNOWN, NEAR);

        assertEquals(NEAR, state.zone);
    }

    @Test
    public void onTransition_does_not_update_zone_on_unknown() {
        AthleteState state = mDisconnectManager.getCurrentState();
        assertNull(state.zone);

        sendTransition(UNKNOWN, FAR); // sets zone to FAR
        sendTransition(FAR, UNKNOWN); // should not change zone

        assertEquals(FAR, state.zone);
    }

    private void failIfPublishMissedLaps() {
        SimpleMessageReceiver.MessageHandler onMissedLaps = new SimpleMessageReceiver.MessageHandler() {
            @Override
            public void onMessage(Intent message) {
                fail("Was not expecting a published lap.");
            }
        };

        mReceiver.registerHandler(DisconnectManager.ACTION_MISSED_LAPS, onMissedLaps);
        CustomAssertions.waitBeforeAssert(500);
    }

    private void testEarlyReturn(LocationStateMachine.State before,
                                 LocationStateMachine.State after) {
        assertNull(mDisconnectManager.getReconnectFunc());
        sendDisconnect();
        failIfPublishMissedLaps();
        sendTransition(before, after);
    }

    private void sendDisconnect() {
        mBroadcastManager.sendBroadcast(new Intent(BLEComm.ACTION_DISCONNECTED));
    }

    private void sendReconnect() {
        mBroadcastManager.sendBroadcast(new Intent(BLEComm.ACTION_RECONNECTED));
    }

    @Test
    public void onTransition_early_return_when_no_prior_disconnect() {
        testEarlyReturn(UNKNOWN, FAR);
    }

    @Test
    public void onTransition_early_return_near_to_far() {
        testEarlyReturn(NEAR, FAR);
    }

    @Test
    public void onTransition_early_return_far_to_near() {
        testEarlyReturn(FAR, NEAR);
    }

    @Test
    public void onTransition_early_return_near_to_unknown() {
        testEarlyReturn(NEAR, UNKNOWN);
    }

    @Test
    public void onTransition_early_return_far_to_unknown() {
        testEarlyReturn(FAR, UNKNOWN);
    }

    @Test
    public void onTransition_calls_computeLapsMissed_does_not_publish() {
        ReconnectFunction rf = mock(ReconnectFunction.class);
        when(rf.computeLapsMissed(any(AthleteState.class))).thenReturn(false);
        mDisconnectManager.setReconnectFunc(rf);

        failIfPublishMissedLaps();

        sendTransition(UNKNOWN, NEAR);

        assertNull(mDisconnectManager.getReconnectFunc());
    }

    @Test
    public void onTransition_calls_computeLapsMissed_does_publish() {
        ReconnectFunction rf = mock(ReconnectFunction.class);
        when(rf.computeLapsMissed(any(AthleteState.class))).thenReturn(true);
        mDisconnectManager.setReconnectFunc(rf);

        class MH implements SimpleMessageReceiver.MessageHandler
        {
            private boolean gotPublishedLap = false;

            @Override
            public void onMessage(Intent message) {
                gotPublishedLap = true;
            }

        }

        MH onMissedLaps = new MH();
        mReceiver.registerHandler(DisconnectManager.ACTION_MISSED_LAPS, onMissedLaps);

        sendTransition(UNKNOWN, NEAR);

        CustomAssertions.waitBeforeAssert(500);

        assertTrue(onMissedLaps.gotPublishedLap);
        assertNull(mDisconnectManager.getReconnectFunc());
    }

    @Test
    public void onDisconnect_early_returns_when_reconnect_func_is_not_null() {
        assertNull(mDisconnectManager.getReconnectFunc());

        // First disconnect will initialize reconnect func.
        sendDisconnect();
        ReconnectFunction rf = mDisconnectManager.getReconnectFunc();
        assertNotNull(rf);

        // Second disconnect should cause an early return.
        sendDisconnect();

        // So the reconnect func remains unchanged.
        assertEquals(rf, mDisconnectManager.getReconnectFunc());
    }

    @Test
    public void onDisconnect_saves_timestamp_to_current_athlete_state() {
        long timestamp = System.currentTimeMillis();
        long prevTimestamp = mDisconnectManager.getCurrentState().timestamp;

        sendDisconnect();

        long newTimestamp = mDisconnectManager.getCurrentState().timestamp;

        // Has the timestamp been updated?
        assertNotEquals(newTimestamp, prevTimestamp);

        // Is the timestamp (roughly; within 1 sec) correct?
        assertTrue(Math.abs(newTimestamp - timestamp) <= 1);
    }

    @Test
    public void onReconnect_saves_timestamp() {
        // It's a one-line method; not much else to test.
        long timestamp = System.currentTimeMillis();
        long prevTimestamp = mDisconnectManager.getCurrentState().timestamp;

        sendReconnect();

        long newTimestamp = mDisconnectManager.getCurrentState().timestamp;

        // Has the timestamp been updated?
        assertNotEquals(newTimestamp, prevTimestamp);

        // Is the timestamp (roughly; within 1 sec) correct?
        assertTrue(Math.abs(newTimestamp - timestamp) <= 1);
    }
}