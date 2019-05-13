package edu.drexel.lapcounter.lapcounter.backend.Database.State;

import android.app.Application;
import android.support.test.InstrumentationRegistry;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import edu.drexel.lapcounter.lapcounter.backend.Database.Transition.TransitionRepository;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

public class StateDBTest
{

    private TransitionRepository transition_repo;
    private State near_state,far_state,unknown_state;
    private int wait_time = 300;



    @Before
    public void setUp() throws Exception
    {
        Application app = (Application) InstrumentationRegistry.getTargetContext().getApplicationContext();
        transition_repo = new TransitionRepository(app);
        near_state = new State(State.NEAR);
        far_state = new State(State.FAR);
        unknown_state = new State(State.UNKNOWN);

        transition_repo.deleteState(near_state);
        transition_repo.deleteState(far_state);
        transition_repo.deleteState(unknown_state);
        Thread.sleep(wait_time);

    }

    @After
    public void cleanUp() throws Exception
    {
        transition_repo.deleteState(near_state);
        transition_repo.deleteState(far_state);
        transition_repo.deleteState(unknown_state);
        Thread.sleep(wait_time);
    }

    @Test
    public void insert_getNumStates() throws Exception
    {
        transition_repo.insert(near_state);
        transition_repo.insert(far_state);
        transition_repo.insert(unknown_state);
        Thread.sleep(wait_time);
        int count = transition_repo.getNumStates();
        assertEquals(3,count);
    }

    @Test
    public void insert_retrieve_State() throws Exception
    {
        transition_repo.insert(near_state);
        Thread.sleep(wait_time);
        List<State> retrieved = transition_repo.getAllStates();
        assertEquals(1,retrieved.size());
        State ret_state = retrieved.get(0);
        assertTrue(ret_state.getStateName().equals(State.NEAR));
    }

    @Test
    public void insert_delete_State() throws Exception
    {
        transition_repo.insert(near_state);
        Thread.sleep(wait_time);
        int count = transition_repo.getNumStates();
        assertEquals(1,count);
        transition_repo.deleteState(near_state);
        Thread.sleep(wait_time);
        count = transition_repo.getNumStates();
        assertEquals(0,count);
    }

    @Test
    public void init_States() throws Exception
    {
        transition_repo.initStatesTable();
        Thread.sleep(wait_time);
        int count = transition_repo.getNumStates();
        List<State> retrieved = transition_repo.getAllStates();
        assertEquals(3,count);
        boolean have_near = false, have_far = false, have_unknown= false;
        for(State s : retrieved)
        {
            if(s.getStateName().equals(State.FAR))
            {
                have_far = true;
            }

            if(s.getStateName().equals(State.NEAR))
            {
                have_near = true;
            }

            if(s.getStateName().equals(State.UNKNOWN))
            {
                have_unknown = true;
            }
        }
        assertTrue(have_far && have_near && have_unknown);
    }
}
