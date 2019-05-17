package edu.drexel.lapcounter.lapcounter.backend.Database.Transition;

import android.app.Application;
import android.os.AsyncTask;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import edu.drexel.lapcounter.lapcounter.backend.Database.LapCounterDatabase;
import edu.drexel.lapcounter.lapcounter.backend.Database.State.State;
import edu.drexel.lapcounter.lapcounter.backend.Database.State.StateDao;

/**
 * TransitionRepository class for interacting with database using state and transition DAO.
 * Allows caller to asynchronously touch database using possible queries specified in DAOs
 * @see StateDao
 * @see TransitionDao
 * @see State
 * @see Transition
 * @see LapCounterDatabase
 */
public class TransitionRepository
{
    /**
     * The state dao used for interacting with DB.
     */
    private StateDao mStateDao;
    /**
     * the Transition dao used for interacting with DB.
     */
    private TransitionDao mTransitionDao;

    /**
     * Constructor for TransitionRepository
     * gets the database, and sets its dao's using DB.
     * @param application Application to get database from.
     */
    public TransitionRepository(Application application)
    {
        LapCounterDatabase db = LapCounterDatabase.getDatabase(application);
        mStateDao = db.stateDao();
        mTransitionDao = db.transitionDao();
    }

    /**
     * Gets all of the States currently stored in database.
     * @return List of all States in DB.
     * @throws InterruptedException
     * @throws ExecutionException
     */
    public List<State> getAllStates() throws InterruptedException, ExecutionException
    {
        ExecutorService ex = Executors.newSingleThreadExecutor();
        Future<List<State>> res = ex.submit(new Callable<List<State>>() {
            @Override
            public List<State> call() throws Exception {
                return mStateDao.getAllStates();
            }
        });
        return res.get();
    }

    /**
     * Gets the number of states currently stored in database, returned as int
     * @return int value of total number of states in DB
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public int getNumStates() throws ExecutionException, InterruptedException {
        ExecutorService ex = Executors.newSingleThreadExecutor();
        Future<Integer> res = ex.submit(new Callable<Integer>() {
            @Override
            public Integer call() {
                return mStateDao.getNumRows();
            }
        });
        return res.get();
    }


    /**
     * insert given State into the database.
     * @param state state to insert.
     */
    public void insert(State state)
    {
        new insertStateAsyncTask(mStateDao).execute(state);
    }
    /**
     * ASyncTask used for State insertion.
     * Allows for DB usage off of UI thread.
     */
    private static class insertStateAsyncTask extends AsyncTask<State,Void,Void>
    {
        private StateDao mAsyncTaskDao;

        insertStateAsyncTask(StateDao dao) {mAsyncTaskDao = dao;}

        @Override
        protected  Void doInBackground(final State... params)
        {
            mAsyncTaskDao.insert(params[0]);
            return null;
        }
    }

    /**
     * deletes given State from database.
     * @param state state to delete.
     */
    public void deleteState(State state)
    {
        new DeleteStateASyncTask(mStateDao).execute(state);
    }
    /**
     * ASyncTask used for State deletion.
     * Allows for DB usage off of UI thread.
     */
    private static class DeleteStateASyncTask extends AsyncTask<State,Void,Void>
    {
        private StateDao mAsyncTaskDao;

        DeleteStateASyncTask(StateDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final State... params) {
            mAsyncTaskDao.deleteState(params[0]);
            return null;
        }
    }

    /**
     * Initialize the States table, to contain NEAR, FAR, and UNKNOWN
     * This is used to ensure that the three states are always in the database.
     */
    public void initStatesTable() throws ExecutionException, InterruptedException {
        // First, let's check if the table already is full. If so, we can stop.
        if (getNumStates() > 0)
            return;

        // The table is empty, so populate it:
        for (String state_name : State.STATE_VALUES) {
            State state = new State(state_name);
            insert(state);
        }
    }

    /**
     * inserts given transition into the database.
     * @param transition transition to insert.
     */
    public void insert(Transition transition) {
        new InsertTransitionTask(mTransitionDao).execute(transition);
    }

    /**
     * ASyncTask used for transition insertion.
     * Allows for DB usage off of UI thread.
     */
    private static class InsertTransitionTask extends AsyncTask<Transition, Void, Void> {
        private TransitionDao mTransitionDao;

        public InsertTransitionTask(TransitionDao dao) {
            mTransitionDao = dao;
        }

        @Override
        protected Void doInBackground(Transition... transitions) {
            mTransitionDao.insertTransition(transitions[0]);
            return null;
        }
    }

    /**
     * Gets a list of all transitions that exist in database.
     * @return List of all transitions in database.
     * @throws InterruptedException
     * @throws ExecutionException
     */
    public List<Transition> getAllTransitions() throws InterruptedException, ExecutionException
    {
        ExecutorService ex = Executors.newSingleThreadExecutor();
        Future<List<Transition>> res = ex.submit(new Callable<List<Transition>>() {
            @Override
            public List<Transition> call(){
                return mTransitionDao.getAllTransitions();
            }
        });
        return res.get();
    }
}
