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

public class TransitionRepository
{
    private StateDao mStateDao;
    private TransitionDao mTransitionDao;

    public TransitionRepository(Application application)
    {
        LapCounterDatabase db = LapCounterDatabase.getDatabase(application);
        mStateDao = db.stateDao();
        mTransitionDao = db.transitionDao();
    }

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


    public void insert(State state)
    {
        new insertStateAsyncTask(mStateDao).execute(state);
    }
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

    public void deleteState(State state)
    {
        new DeleteStateASyncTask(mStateDao).execute(state);
    }
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
     * Initialize the States table
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

    public void insert(Transition transition) {
        new InsertTransitionTask(mTransitionDao).execute(transition);
    }
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
