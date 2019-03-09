package edu.drexel.lapcounter.lapcounter.backend.Database;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;
import android.util.Log;

import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class WorkoutRepository {

    private WorkoutDao mWorkoutDao;
    private LiveData<List<Workouts>> mAllWorkouts;
    private List<Workouts> Workouts;




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

    public List<Workouts> findWorkoutsBetweenDates(final Date date1,final Date date2){

        ExecutorService ex = Executors.newSingleThreadExecutor();
        Future<List<Workouts>> res = ex.submit(new Callable<List<Workouts>>() {
            @Override
            public List<Workouts> call() throws Exception {
                return mWorkoutDao.findWorkoutsBetweenDates(date1,date2);
            }
        });
        return null;
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
