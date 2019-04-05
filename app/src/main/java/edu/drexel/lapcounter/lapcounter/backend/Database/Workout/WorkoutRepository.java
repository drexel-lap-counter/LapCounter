package edu.drexel.lapcounter.lapcounter.backend.Database.Workout;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;
import android.util.Log;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class WorkoutRepository {

    private WorkoutDao mWorkoutDao;
    private LiveData<List<Workouts>> mAllWorkouts;


    WorkoutRepository(Application application) {
        WorkoutDatabase db = WorkoutDatabase.getDatabase(application);
        mWorkoutDao = db.workoutDao();
        mAllWorkouts = mWorkoutDao.getAllWorkouts();
    }

    LiveData<List<Workouts>> getAllWorkouts() {
        return mAllWorkouts;
    }

    public void insert(Workouts workout)
    {
        new insertAsyncTask(mWorkoutDao).execute(workout);
    }

    public Workouts getWorkoutByID(final int id)
    {
        ExecutorService ex = Executors.newSingleThreadExecutor();
        Future<Workouts> res = ex.submit(new Callable<Workouts>() {
            @Override
            public Workouts call() throws Exception {

                return mWorkoutDao.getWorkoutByID(id);
            }
        });
        Workouts test = new Workouts();
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


    private static class insertAsyncTask extends AsyncTask<Workouts,Void,Void>
    {
        private WorkoutDao mAsyncTaskDao;

        insertAsyncTask(WorkoutDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Workouts... params) {
            mAsyncTaskDao.addWorkout(params[0]);
            return null;
        }
    }

}
