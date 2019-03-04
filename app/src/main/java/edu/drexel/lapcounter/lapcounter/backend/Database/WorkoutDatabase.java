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
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(), WorkoutDatabase.class, "workoutsdb").build();
                }
            }
        }
        return INSTANCE;
    }


    /*private static RoomDatabase.Callback sRoomDatabaseCallback = new RoomDatabase.Callback()
    {
        public void onOpen(@NonNull SupportSQLiteDatabase db)
        {
            super.onOpen(db);
            new PopulateDbAsync(INSTANCE).execute();
        }
    };

    private static class PopulateDbAsync extends AsyncTask<Void, Void, Void> {
        private final WorkoutDao mDao;

        PopulateDbAsync(WorkoutDatabase db) {
            mDao = db.workoutDao();
        }


        @Override
        protected Void doInBackground(final Void... params) {
            // Start the app with a clean database every time.
            // Not needed if you only populate on creation.\
            int initialPoolLength = 50;
            int initialTotalDistanceTraveled = 1700;


            mDao.deleteAll();

            Workouts workout = new Workouts();

            workout.setID(55);
            workout.setPoolLength(initialPoolLength);
            workout.setTotalDistanceTraveled(initialTotalDistanceTraveled);
            workout.setStartDateTime(TimestampConverter.fromTimestamp("1999-12-27 23:00:00.000"));
            workout.setEndDateTime(TimestampConverter.fromTimestamp("1999-12-27 23:30:00.000"));
            workout.setLaps(50);
            workout.setPoolUnits("Meters");
            mDao.addWorkout(workout);
            return null;
        }
    }*/


}
