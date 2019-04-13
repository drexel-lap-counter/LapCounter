package edu.drexel.lapcounter.lapcounter.backend.Database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

import edu.drexel.lapcounter.lapcounter.backend.Database.Device.Device;
import edu.drexel.lapcounter.lapcounter.backend.Database.Device.DeviceDao;
import edu.drexel.lapcounter.lapcounter.backend.Database.Workout.Workout;
import edu.drexel.lapcounter.lapcounter.backend.Database.Workout.WorkoutDao;


@Database(entities = {Workout.class, Device.class}, version = 1)
public abstract class LapCounterDatabase extends RoomDatabase {


    public abstract WorkoutDao workoutDao();
    public abstract DeviceDao deviceDao();

    private static volatile LapCounterDatabase INSTANCE;
    private final static String mDatabaseName = "lapcounterdb";

    public static LapCounterDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (LapCounterDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(), LapCounterDatabase.class, mDatabaseName).build();
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

            Workout workout = new Workout();

            workout.setID(55);
            workout.setPoolLength(initialPoolLength);
            workout.setTotalDistanceTraveled(initialTotalDistanceTraveled);
            workout.setStartDate(TimestampConverter.fromTimestamp("1999-12-27 23:00:00.000"));
            workout.setEndDate(TimestampConverter.fromTimestamp("1999-12-27 23:30:00.000"));
            workout.setLaps(50);
            workout.setPoolUnits("Meters");
            mDao.addWorkout(workout);
            return null;
        }
    }*/


}
