package edu.drexel.lapcounter.lapcounter.backend.lapcounter;

import org.junit.Test;

import static edu.drexel.lapcounter.lapcounter.backend.ble.RSSIManager.DIRECTION_IN;
import static org.junit.Assert.*;

public class AthleteStateTest {
    @Test
    public void copy_successfully_copies_default_state() {
        AthleteState state = new AthleteState();
        AthleteState stateCopy = state.copy();

        assertEquals(state, stateCopy);
        assertEquals(state.hashCode(), stateCopy.hashCode());
    }

    @Test
    public void copy_successfully_copies_non_default_state() {
        AthleteState state = new AthleteState();
        state.zone = LocationStateMachine.State.FAR;
        state.distRssi = 83;
        state.travelDirection = DIRECTION_IN;
        state.timestamp = 123456789;

        AthleteState stateCopy = state.copy();

        assertEquals(state, stateCopy);
        assertEquals(state.hashCode(), stateCopy.hashCode());
    }
}