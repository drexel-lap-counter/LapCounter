package edu.drexel.lapcounter.lapcounter.backend.Database.State;

import org.junit.Before;
import org.junit.Test;

import edu.drexel.lapcounter.lapcounter.backend.SimpleMessageReceiver;

import static org.junit.Assert.*;

public class StateTest {

    State state;
    @Before
    public void setUp() throws Exception {
        state = new State();
    }

    @Test
    public void getStateName_retrieves_setStateName() {
        String name = "foo";
        state.setStateName(name);

        assertEquals(name, state.getStateName());
    }
}