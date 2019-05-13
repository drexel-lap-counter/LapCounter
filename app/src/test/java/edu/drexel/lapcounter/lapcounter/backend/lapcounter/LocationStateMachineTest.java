package edu.drexel.lapcounter.lapcounter.backend.lapcounter;

import android.content.Context;
import android.content.Intent;

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

import static org.mockito.Mockito.mock;

@RunWith(RobolectricTestRunner.class)
public class LocationStateMachineTest {
    private SimpleMessageReceiver mReceiver;
    private IBroadcastManager mBroadcastManager;

    private final int THRESHOLD = 75;

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

        LocationStateMachine mMachine = new LocationStateMachine(mBroadcastManager, 0);
        mMachine.setThreshold(THRESHOLD);
        mMachine.initCallbacks(mReceiver);
    }

    private void sendRssiAndDir(double rssi, int direction) {
        Intent intent = new Intent(RSSIManager.ACTION_RSSI_AND_DIR_AVAILABLE);
        intent.putExtra(RSSIManager.EXTRA_RSSI, rssi);
        intent.putExtra(RSSIManager.EXTRA_DIRECTION, direction);
        mBroadcastManager.sendBroadcast(intent);
    }

    private void sendDisconnect() {
        mBroadcastManager.sendBroadcast(new Intent(BLEComm.ACTION_DISCONNECTED));
    }

    class MH implements SimpleMessageReceiver.MessageHandler
    {
        private LocationStateMachine.State before;
        private LocationStateMachine.State after;

        private LocationStateMachine.State deserialize(Intent intent, String extra) {
            return (LocationStateMachine.State) intent.getSerializableExtra(extra);
        }

        @Override
        public void onMessage(Intent message) {
            before = deserialize(message, LocationStateMachine.EXTRA_STATE_BEFORE);
            after = deserialize(message, LocationStateMachine.EXTRA_STATE_AFTER);
        }
    }

    public MH setupHandler() {
        MH onStateTransition = new MH();
        mReceiver.registerHandler(LocationStateMachine.ACTION_STATE_TRANSITION, onStateTransition);
        return onStateTransition;
    }

    @Test
    public void onRssiAndDirection_first_rssi_near() {
        MH handler = setupHandler();

        sendRssiAndDir(THRESHOLD - 10, 0);

        assertEquals(handler.before, UNKNOWN);
        assertEquals(handler.after, NEAR);
    }

    @Test
    public void onRssiAndDirection_first_rssi_far() {
        MH handler = setupHandler();

        sendRssiAndDir(THRESHOLD + 10, 0);

        assertEquals(handler.before, UNKNOWN);
        assertEquals(handler.after, FAR);
    }

    @Test
    public void onRssiAndDirection_first_rssi_threshold() {
        MH handler = setupHandler();

        sendRssiAndDir(THRESHOLD, 0);

        assertEquals(handler.before, UNKNOWN);
        assertEquals(handler.after, NEAR);
    }

    @Test
    public void onRssiAndDirection_near_to_far() {
        MH handler = setupHandler();

        sendRssiAndDir(THRESHOLD - 10, 0);

        // We're first in the near zone.
        assertEquals(handler.before, UNKNOWN);
        assertEquals(handler.after, NEAR);

        // Then we cross the threshold, moving away from the phone.
        sendRssiAndDir(THRESHOLD + 10, 1);

        // So now we must be in the far zone.
        assertEquals(handler.before, NEAR);
        assertEquals(handler.after, FAR);
    }

    @Test
    public void onRssiAndDirection_far_to_near() {
        MH handler = setupHandler();

        sendRssiAndDir(THRESHOLD + 10, 0);

        // We're first in the far zone.
        assertEquals(handler.before, UNKNOWN);
        assertEquals(handler.after, FAR);

        // Then we cross the threshold, moving towards the phone.
        sendRssiAndDir(THRESHOLD - 10, -1);

        // So now we must be in the near zone.
        assertEquals(handler.before, FAR);
        assertEquals(handler.after, NEAR);
    }

    @Test
    public void onRssiAndDirection_near_to_near() {
        MH handler = setupHandler();

        sendRssiAndDir(THRESHOLD - 10, 0);

        // We're first in the near zone.
        assertEquals(handler.before, UNKNOWN);
        assertEquals(handler.after, NEAR);

        // Then we get closer to the threshold without crossing it.
        sendRssiAndDir(THRESHOLD - 5, 1);

        // So we must still be in the near zone.
        assertEquals(handler.before, UNKNOWN);
        assertEquals(handler.after, NEAR);
    }

    @Test
    public void onRssiAndDirection_far_to_far() {
        MH handler = setupHandler();

        sendRssiAndDir(THRESHOLD + 10, 0);

        // We're first in the far zone.
        assertEquals(handler.before, UNKNOWN);
        assertEquals(handler.after, FAR);

        // Then we get closer to the threshold without crossing it.
        sendRssiAndDir(THRESHOLD + 5, -1);

        // So we must still be in the far zone.
        assertEquals(handler.before, UNKNOWN);
        assertEquals(handler.after, FAR);
    }

    @Test
    public void onRssiAndDirection_near_to_far_inwards() {
        MH handler = setupHandler();

        sendRssiAndDir(THRESHOLD - 10, 0);

        // We're first in the near zone.
        assertEquals(handler.before, UNKNOWN);
        assertEquals(handler.after, NEAR);

        // Then we cross the threshold, somehow moving toward the phone.
        sendRssiAndDir(THRESHOLD + 10, -1);

        // Since we were moving inwards, we must still be in the near zone.
        assertEquals(handler.before, UNKNOWN);
        assertEquals(handler.after, NEAR);
    }

    @Test
    public void onRssiAndDirection_far_to_near_outwards() {
        MH handler = setupHandler();

        sendRssiAndDir(THRESHOLD + 10, 0);

        // We're first in the far zone.
        assertEquals(handler.before, UNKNOWN);
        assertEquals(handler.after, FAR);

        // Then we cross the threshold, somehow moving away from the phone.
        sendRssiAndDir(THRESHOLD - 10, 1);

        // Since we were moving outwards, we must still be in the far zone.
        assertEquals(handler.before, UNKNOWN);
        assertEquals(handler.after, FAR);
    }

    @Test
    public void onDisconnect_unknown_to_unknown() {
        MH handler = setupHandler();

        sendDisconnect();

        assertEquals(handler.before, UNKNOWN);
        assertEquals(handler.after, UNKNOWN);
    }

    @Test
    public void onDisconnect_near_to_unknown() {
        MH handler = setupHandler();

        sendRssiAndDir(THRESHOLD - 10, 0);

        // We're first in the near zone.
        assertEquals(handler.before, UNKNOWN);
        assertEquals(handler.after, NEAR);

        sendDisconnect();

        assertEquals(handler.before, NEAR);
        assertEquals(handler.after, UNKNOWN);
    }

    @Test
    public void onDisconnect_far_to_unknown() {
        MH handler = setupHandler();

        sendRssiAndDir(THRESHOLD + 10, 0);

        // We're first in the far zone.
        assertEquals(handler.before, UNKNOWN);
        assertEquals(handler.after, FAR);

        sendDisconnect();

        assertEquals(handler.before, FAR);
        assertEquals(handler.after, UNKNOWN);
    }

    @Test
    public void onDisconnect_unknown_to_near() {
        MH handler = setupHandler();

        sendDisconnect();

        assertEquals(handler.before, UNKNOWN);
        assertEquals(handler.after, UNKNOWN);

        sendRssiAndDir(THRESHOLD - 10, 0);

        assertEquals(handler.before, UNKNOWN);
        assertEquals(handler.after, NEAR);
    }

    @Test
    public void onDisconnect_unknown_to_far() {
        MH handler = setupHandler();

        sendDisconnect();

        assertEquals(handler.before, UNKNOWN);
        assertEquals(handler.after, UNKNOWN);

        sendRssiAndDir(THRESHOLD + 10, 0);

        assertEquals(handler.before, UNKNOWN);
        assertEquals(handler.after, FAR);
    }
}