package edu.drexel.lapcounter.lapcounter.backend.Database.Workout;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.RoomDatabase;
import android.os.AsyncTask;
import android.util.Log;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import edu.drexel.lapcounter.lapcounter.backend.Database.Device.DeviceDao;
import edu.drexel.lapcounter.lapcounter.backend.Database.LapCounterDatabase;
import edu.drexel.lapcounter.lapcounter.backend.Database.Units.Units;
import edu.drexel.lapcounter.lapcounter.backend.Database.Units.UnitsDao;

public class WorkoutRepository {

    private WorkoutDao mWorkoutDao;
    private UnitsDao mUnitsDao;


    WorkoutRepository(Application application) {
        LapCounterDatabase db = LapCounterDatabase.getDatabase(application);
        mWorkoutDao = db.workoutDao();
        mUnitsDao = db.unitsDao();
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
            public List<Units> call() throws Exception {

                return mUnitsDao.getAllUnits();
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
        new InsertDeleteUnitsASyncTask(mUnitsDao).execute(units);
    }
    private static class InsertDeleteUnitsASyncTask extends AsyncTask<Units,Void,Void>
    {
        private UnitsDao mAsyncTaskDao;

        InsertDeleteUnitsASyncTask(UnitsDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Units... params) {
            mAsyncTaskDao.deleteUnits(params[0]);
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
        new insertAsyncTask(mWorkoutDao).execute(workout);
    }
    private static class insertAsyncTask extends AsyncTask<Workout,Void,Void>
    {
        private WorkoutDao mAsyncTaskDao;

        insertAsyncTask(WorkoutDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Workout... params) {
            mAsyncTaskDao.addWorkout(params[0]);
            return null;
        }
    }

    public void deleteAll()
    {
        new DeleteAllAsyncTask(mWorkoutDao).execute();
    }
    private static class DeleteAllAsyncTask extends AsyncTask<Void,Void,Void>
    {
        private WorkoutDao mAsyncTaskDao;
        DeleteAllAsyncTask(WorkoutDao dao) {mAsyncTaskDao = dao;}

        @Override
        protected Void doInBackground(Void...params)
        {
            mAsyncTaskDao.deleteAll();
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
}
