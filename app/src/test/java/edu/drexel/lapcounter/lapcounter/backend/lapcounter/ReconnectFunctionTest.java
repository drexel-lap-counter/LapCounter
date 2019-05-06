package edu.drexel.lapcounter.lapcounter.backend.lapcounter;

import org.junit.Before;
import org.junit.Test;

import edu.drexel.lapcounter.lapcounter.backend.Hyperparameters;

import static edu.drexel.lapcounter.lapcounter.backend.lapcounter.LocationStateMachine.State.FAR;
import static edu.drexel.lapcounter.lapcounter.backend.lapcounter.LocationStateMachine.State.NEAR;
import static org.junit.Assert.*;

public class ReconnectFunctionTest {

    private ReconnectFunction mReconnectFunc;

    @Before
    public void setup() {
        mReconnectFunc = new ReconnectFunction();
    }

    private AthleteState transition(LocationStateMachine.State before,
                                    LocationStateMachine.State after, long delaySec) {

        AthleteState b = new AthleteState();
        b.zone = before;
        b.timestamp = 0;
        mReconnectFunc.setBeforeState(b);

        AthleteState a = new AthleteState();
        a.timestamp = b.timestamp + 1000 * delaySec;
        a.zone = after;
        return a;
    }

    private AthleteState transition(LocationStateMachine.State before,
                                    LocationStateMachine.State after) {
        return transition(before, after, 0);
    }

    @Test
    public void computeLapsMissed_far_to_near() {
        assertTrue(mReconnectFunc.computeLapsMissed(transition(FAR, NEAR)));
    }

    @Test
    public void computeLapsMissed_near_to_near_long_delay() {
        assertTrue(mReconnectFunc.computeLapsMissed(transition(NEAR, NEAR,
                Hyperparameters.RECONNECT_LONG_DELAY_THRESHOLD_SEC)));
    }

    @Test
    public void computeLapsMissed_far_to_far() {
        assertFalse(mReconnectFunc.computeLapsMissed(transition(FAR, FAR)));
    }

    @Test
    public void computeLapsMissed_near_to_near_short_delay() {
        assertFalse(mReconnectFunc.computeLapsMissed(transition(NEAR, NEAR)));
    }
}