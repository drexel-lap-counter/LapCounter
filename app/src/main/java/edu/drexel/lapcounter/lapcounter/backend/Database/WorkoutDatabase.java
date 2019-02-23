package edu.drexel.lapcounter.lapcounter.backend.Database;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.*;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;


@Database(entities = {Workouts.class}, version = 1)
public abstract class WorkoutDatabase extends RoomDatabase {


    public abstract WorkoutDao workoutDao();


    private static volatile WorkoutDatabase INSTANCE;

    static WorkoutDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (WorkoutDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(), WorkoutDatabase.class, "userdb").allowMainThreadQueries().build();
                }
            }
        }
            return INSTANCE;
    }



//    private static class PopulateDbAsync extends AsyncTask<Void, Void, Void> {
////
////        private final WorkoutDao mDao;
////
////        PopulateDbAsync(WorkoutDatabase db) {
////            mDao = db.workoutDao();
////        }
////
////
////
////        @Override
////        protected Void doInBackground(final Void... params) {
////            // Start the app with a clean database every time.
////            // Not needed if you only populate on creation.\
////            int initialID = 1;
////            int initialPoolLength = 50;
////            int initialTotalDistanceTraveled = 100;
////            int initialAvgWorkoutDistance = 100;
////
////
////            mDao.deleteAll();
////
//            Workouts workout = new Workouts();
//
//            workout.setID(initialID);
//            workout.setPoolLength(initialPoolLength);
//            workout.setTotalDistanceTraveled(initialTotalDistanceTraveled);
//            workout.setAvgWorkoutDistance(initialAvgWorkoutDistance);
//            mDao.addWorkout(workout);
//            return null;
////        }


}

