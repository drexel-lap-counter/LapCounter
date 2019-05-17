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

/**
 * WorkoutRepository class for interacting with Workout and Units DAO to touch database.
 * If you are in a service, use this to interact with db.
 * If you are an activity, use the WorkoutViewModel instead.
 * Allows caller to asynchronously touch database using possible queries specified in Daos.
 */
public class WorkoutRepository {

    /**
     * The Workout DAO used for interacting with workouts table.
     */
    private WorkoutDao mWorkoutDao;
    /**
     * The Units DAO used for interacting with units table.
     */
    private UnitsDao mUnitsDao;
    /**
     * Application that created the database.
     */
    private Application mApp;


    /**
     * Constructor for Workout Repository.
     * gets the database, and sets its daos using DB.
     * @param application Application to get database from.
     */
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
    /**
     * Gets all of the Units stored within the database.
     * gets list of all units, or empty list if there is none.
     * @return List of Units for all units in database, or empty list
     * @throws InterruptedException
     * @throws ExecutionException
     */
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

    /**
     * Gets the total number of Units stored within the database.
     * @return int value for number of Units entries in database.
     * @throws ExecutionException
     * @throws InterruptedException
     */
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

    /**
     * Inserts given Units into the database.
     * @param units Units to insert.
     */
    public void insert(Units units)
    {
        new insertUnitsAsyncTask(mUnitsDao).execute(units);
    }
    /**
     * ASyncTask used for Units insertion.
     * Allows for DB usage off of UI thread.
     */
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

    /**
     * Deletes given units from database if it exists in it.
     * @param units Units to delete from database.
     */
    public void deleteUnits(Units units)
    {
        new DeleteUnitsASyncTask(mUnitsDao).execute(units);
    }
    /**
     * ASyncTask used for Units deletion.
     * Allows for DB usage off of UI thread.
     */
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

    /**
     * Deletes all units from the database.
     */
    public void deleteAllUnits()
    {
        new DeleteAllUnitsAsyncTask(mUnitsDao).execute();
    }
    /**
     * ASyncTask used for all Units deletion.
     * Allows for DB usage off of UI thread.
     */
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
    /**
     * Gets all of the workouts currently stored in database
     * @return List of all workouts in DB.  Will be an empty list if there are no workouts in DB.
     */
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

    /**
     * Gets the workout in database with given id if it exists.
     * if it does not exist, it will return null.
     * @param id int id of workout to look for.
     * @return Workout object with id if it is found, else null.
     */
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

    /**
     * Gets the workouts in database who were completed between given start and end time.
     * If there are none in the range, empty list is returned.
     * @param start_time Date object of start time and date of range
     * @param end_time Date object of end time and date of range
     * @return List of Workouts within range, or empty list if there is none.
     * @throws Exception
     */
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

    /**
     * Insert a workout into the database if it is not present, else replace.
     * @param workout Workout object to insert or replace.
     */
    public void insert(Workout workout)
    {
        new InsertAsyncTask(mWorkoutDao, mApp).execute(workout);
    }
    /**
     * ASyncTask used for workout insertion.
     * Also passes the ID to transition log once inserted for use in lap counting logic
     * Allows for DB usage off of UI thread.
     */
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

    /**
     * Deletes all workouts that are within the database
     */
    public void deleteAllWorkouts()
    {
        new DeleteAllWorkoutsAsyncTask(mWorkoutDao).execute();
    }

    /**
     * ASyncTask used for deleting all workouts.
     * Allows for DB usage off of UI thread.
     */
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

    /**
     * Deletes workout with given id from database if it is present.
     * @param id  int id of workout to look for.
     */
    public void deleteWorkoutByID(int id)
    {
        new DeleteIDAsyncTask(mWorkoutDao).execute(id);
    }

    /**
     * ASyncTask used for workout deletion.
     * Allows for DB usage off of UI thread.
     */
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

    /**
     * Gets all workouts within the database, and returns them in a list descending by start date.
     * Returns a list of all workouts in database in descending format based on the start date.
     * If there are no values in database, returns empty list
     * @return List of Workouts in descending order, or empty list
     */
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
     * Initialize the Units table.
     * Used to make sure that all required Units are in the database.
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
