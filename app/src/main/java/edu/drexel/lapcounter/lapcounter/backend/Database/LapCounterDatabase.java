package edu.drexel.lapcounter.lapcounter.backend.Database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

import edu.drexel.lapcounter.lapcounter.backend.Database.Device.Device;
import edu.drexel.lapcounter.lapcounter.backend.Database.Device.DeviceDao;
import edu.drexel.lapcounter.lapcounter.backend.Database.State.State;
import edu.drexel.lapcounter.lapcounter.backend.Database.State.StateDao;
import edu.drexel.lapcounter.lapcounter.backend.Database.Transition.Transition;
import edu.drexel.lapcounter.lapcounter.backend.Database.Transition.TransitionDao;
import edu.drexel.lapcounter.lapcounter.backend.Database.Units.Units;
import edu.drexel.lapcounter.lapcounter.backend.Database.Units.UnitsDao;
import edu.drexel.lapcounter.lapcounter.backend.Database.Workout.Workout;
import edu.drexel.lapcounter.lapcounter.backend.Database.Workout.WorkoutDao;

/**
 * Database class that contains all of the database DAOS.
 * Class will greate an instance of a RoomDatabase, and will pass DAOS from this instance to
 * Repos.
 * Entities: Workout
 *           Device
 *           Units
 *           State
 *           Transition
 * Database: lapcounterdb
 */
@Database(entities = {
        Workout.class,
        Device.class,
        Units.class,
        State.class,
        Transition.class
}, version = 1)
public abstract class LapCounterDatabase extends RoomDatabase {


    /**
     * Workout DAO used to interact with Workouts table workouts
     * @return WorkoutDAO object to use.
     */
    public abstract WorkoutDao workoutDao();
    /**
     * Device DAO used to interact with Device table devices
     * @return DeviceDAO object to use.
     */
    public abstract DeviceDao deviceDao();
    /**
     * Units DAO used to interact with Units table units_table
     * @return UnitsDAO object to use.
     */
    public abstract UnitsDao unitsDao();
    /**
     * State DAO used to interact with State table states_table
     * @return StateDAO object to use.
     */
    public abstract StateDao stateDao();
    /**
     * Transition DAO used to interact with Trasnition table transitions_table
     * @return TransitionDAO object to use.
     */
    public abstract TransitionDao transitionDao();

    private static volatile LapCounterDatabase INSTANCE;
    /**
     * Name of database
     */
    public final static String DATABASE_NAME = "lapcounterdb";

    /**
     * Creates and returns an instance of the database.
     * If the instance was already created, will return it instead of create a new one.
     * @param context  Context of activity to make build database with.
     * @return LapCounterDatabase object.
     */
    public static LapCounterDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (LapCounterDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(), LapCounterDatabase.class, DATABASE_NAME).build();
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


            mDao.deleteAllWorkouts();

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
