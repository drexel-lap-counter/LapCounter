package edu.drexel.lapcounter.lapcounter.backend.Database.Workout;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import edu.drexel.lapcounter.lapcounter.backend.Database.LapCounterDatabase;
import edu.drexel.lapcounter.lapcounter.backend.Database.Units.Units;
import edu.drexel.lapcounter.lapcounter.backend.Database.Units.UnitsDao;
import edu.drexel.lapcounter.lapcounter.backend.lapcounter.TransitionLog;

public class WorkoutRepository {

    private WorkoutDao mWorkoutDao;
    private UnitsDao mUnitsDao;
    private Application mApp;


    public WorkoutRepository(Application application) {
        LapCounterDatabase db = LapCounterDatabase.getDatabase(application);
        mWorkoutDao = db.workoutDao();
        mUnitsDao = db.unitsDao();
        mApp = application;
    }

    //Attempt to refactor some of the code
    /*private <T> T Execute(Callable<T> callable) throws InterruptedException, ExecutionException
    {
        ExecutorService ex = Executors.newSingleThreadExecutor();
        Future<T> res = ex.submit(callable);

        T output = res.get();
        return output;
    }*/

    /*** UNITS CODE***/
    public List<Units> getAllUnits() throws InterruptedException, ExecutionException
    {
        ExecutorService ex = Executors.newSingleThreadExecutor();
        Future<List<Units>> res = ex.submit(new Callable<List<Units>>() {
            @Override
            public List<Units> call() {

                return mUnitsDao.getAllUnits();
            }
        });
        return res.get();
    }

    public int getNumUnits() throws ExecutionException, InterruptedException {
        ExecutorService ex = Executors.newSingleThreadExecutor();
        Future<Integer> res = ex.submit(new Callable<Integer>() {
            @Override
            public Integer call() {
                return mUnitsDao.getNumRows();
            }
        });
        return res.get();
    }

    public void insert(Units units)
    {
        new insertUnitsAsyncTask(mUnitsDao).execute(units);
    }
    private static class insertUnitsAsyncTask extends AsyncTask<Units,Void,Void>
    {
        private UnitsDao mAsyncTaskDao;

        insertUnitsAsyncTask(UnitsDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Units... params) {
            mAsyncTaskDao.insert(params[0]);
            return null;
        }
    }

    public void deleteUnits(Units units)
    {
        new DeleteUnitsASyncTask(mUnitsDao).execute(units);
    }
    private static class DeleteUnitsASyncTask extends AsyncTask<Units,Void,Void>
    {
        private UnitsDao mAsyncTaskDao;

        DeleteUnitsASyncTask(UnitsDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Units... params) {
            mAsyncTaskDao.deleteUnits(params[0]);
            return null;
        }
    }

    public void deleteAllUnits()
    {
        new DeleteAllUnitsAsyncTask(mUnitsDao).execute();
    }
    private static class DeleteAllUnitsAsyncTask extends AsyncTask<Void,Void,Void>
    {
        private UnitsDao mAsyncTaskDao;
        DeleteAllUnitsAsyncTask(UnitsDao dao) {mAsyncTaskDao = dao;}

        @Override
        protected Void doInBackground(Void...params)
        {
            mAsyncTaskDao.deleteAllUnits();
            return null;
        }
    }


    /*** WORKOUT CODE ***/
    public List<Workout> getAllWorkouts()
    {
        ExecutorService ex = Executors.newSingleThreadExecutor();
        Future<List<Workout>> res = ex.submit(new Callable<List<Workout>>() {
            @Override
            public List<Workout> call() throws Exception {

                return mWorkoutDao.getAllWorkouts();
            }
        });
        List<Workout> workouts = new ArrayList<Workout>();
        try
        {
            workouts = res.get();
        }
        catch(Exception e)
        {
            Log.i("ERROR",e.toString());
        }
        ArrayList<Workout> output = new ArrayList<Workout>();
        output.addAll(workouts);
        return output;
    }
    public Workout getWorkoutByID(final int id)
    {
        ExecutorService ex = Executors.newSingleThreadExecutor();
        Future<Workout> res = ex.submit(new Callable<Workout>() {
            @Override
            public Workout call() throws Exception {

                return mWorkoutDao.getWorkoutByID(id);
            }
        });
        Workout test = new Workout();
        try
        {
            test = res.get();
        }
        catch(Exception e)
        {
            Log.i("ERROR",e.toString());
        }
        return test;

    }

    public List<Workout> getWorkoutsByDateRange(final Date start_time,final Date end_time) throws Exception
    {
        ExecutorService ex = Executors.newSingleThreadExecutor();
        Future<List<Workout>> res = ex.submit(new Callable<List<Workout>>() {
            @Override
            public List<Workout> call() throws Exception {

                return mWorkoutDao.getWorkoutsByDateRange(start_time,end_time);
            }
        });
        List<Workout> workouts = new ArrayList<Workout>();
        try
        {
            workouts = res.get();
        }
        catch(Exception e)
        {
            throw e;
        }
        ArrayList<Workout> output = new ArrayList<Workout>();
        output.addAll(workouts);
        return output;
    }

    public void insert(Workout workout)
    {
        new InsertAsyncTask(mWorkoutDao, mApp).execute(workout);
    }
    private static class InsertAsyncTask extends AsyncTask<Workout,Void,Integer>
    {
        private WorkoutDao mAsyncTaskDao;
        private Application mApp;
        private LocalBroadcastManager mBroadcastManager;

        InsertAsyncTask(WorkoutDao dao, Application app) {
            mAsyncTaskDao = dao;
            mApp = app;
            mBroadcastManager = LocalBroadcastManager.getInstance(mApp);
        }

        @Override
        protected Integer doInBackground(final Workout... params) {
            return (int) mAsyncTaskDao.addWorkout(params[0]);
        }

        @Override
        protected void onPostExecute(Integer workoutID) {
            Intent intent = new Intent(TransitionLog.ACTION_FLUSH_LOG);
            intent.putExtra(TransitionLog.EXTRA_WORKOUT_ID, workoutID);
            mBroadcastManager.sendBroadcast(intent);
        }
    }

    public void deleteAllWorkouts()
    {
        new DeleteAllWorkoutsAsyncTask(mWorkoutDao).execute();
    }
    private static class DeleteAllWorkoutsAsyncTask extends AsyncTask<Void,Void,Void>
    {
        private WorkoutDao mAsyncTaskDao;
        DeleteAllWorkoutsAsyncTask(WorkoutDao dao) {mAsyncTaskDao = dao;}

        @Override
        protected Void doInBackground(Void...params)
        {
            mAsyncTaskDao.deleteAllWorkouts();
            return null;
        }
    }

    public void deleteWorkoutByID(int id)
    {
        new DeleteIDAsyncTask(mWorkoutDao).execute(id);
    }
    private static class DeleteIDAsyncTask extends AsyncTask<Integer,Void,Void>
    {
        private WorkoutDao mAsyncTaskDao;
        DeleteIDAsyncTask(WorkoutDao dao) {mAsyncTaskDao = dao;}

        @Override
        protected Void doInBackground(Integer...ints)
        {
            mAsyncTaskDao.deleteWorkout(ints[0]);
            return null;
        }
    }

    public List<Workout> getAllWorkoutsDecending()
    {
        ExecutorService ex = Executors.newSingleThreadExecutor();
        Future<List<Workout>> res = ex.submit(new Callable<List<Workout>>() {
            @Override
            public List<Workout> call() throws Exception {

                return mWorkoutDao.getAllWorkoutsDecending();
            }
        });
        List<Workout> workouts = new ArrayList<Workout>();
        try
        {
            workouts = res.get();
        }
        catch(Exception e)
        {
            Log.i("ERROR",e.toString());
        }
        ArrayList<Workout> output = new ArrayList<Workout>();
        output.addAll(workouts);
        return output;
    }

    /**
     * Initialize the Units table
     */
    public void initUnitsTable() throws ExecutionException, InterruptedException {
        // First, let's check if the table already is full. If so, we can stop.
        if (getNumUnits() > 0)
            return;

        // The table is empty, so populate it:
        for (String unit_name : Units.UNITS_VALUES) {
            Units unit = new Units(unit_name);
            insert(unit);
        }
    }
}
