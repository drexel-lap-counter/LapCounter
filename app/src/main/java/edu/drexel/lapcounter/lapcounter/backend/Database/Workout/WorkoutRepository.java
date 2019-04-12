package edu.drexel.lapcounter.lapcounter.backend.Database.Workout;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;
import android.util.Log;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import edu.drexel.lapcounter.lapcounter.backend.Database.Device.DeviceDao;
import edu.drexel.lapcounter.lapcounter.backend.Database.LapCounterDatabase;

public class WorkoutRepository {

    private WorkoutDao mWorkoutDao;


    WorkoutRepository(Application application) {
        LapCounterDatabase db = LapCounterDatabase.getDatabase(application);
        mWorkoutDao = db.workoutDao();
    }

    public ArrayList<Workout> getAllWorkouts()
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

    public ArrayList<Workout> getWorkoutsByDateRange(final Date start_time,final Date end_time)
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
            Log.i("ERROR",e.toString());
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
}
