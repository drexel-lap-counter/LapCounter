package edu.drexel.lapcounter.lapcounter.backend.Database.Device;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import edu.drexel.lapcounter.lapcounter.backend.Database.Workout.WorkoutDao;
import edu.drexel.lapcounter.lapcounter.backend.Database.Workout.Workouts;


@Database(entities = {Device.class}, version = 1)
public abstract class DeviceDatabase extends RoomDatabase {


    public abstract DeviceDao deviceDao();


    private static volatile DeviceDatabase INSTANCE;

    public static DeviceDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (DeviceDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(), DeviceDatabase.class, "workoutsdb").build();
                }
            }
        }
        return INSTANCE;
    }


    private static RoomDatabase.Callback sRoomDatabaseCallback = new RoomDatabase.Callback()
    {
        public void onOpen(@NonNull SupportSQLiteDatabase db)
        {
            super.onOpen(db);
            new PopulateDbAsync(INSTANCE).execute();
        }
    };

    private static class PopulateDbAsync extends AsyncTask<Void, Void, Void> {
        private final DeviceDao mDao;

        PopulateDbAsync(DeviceDatabase db) {
            mDao = db.deviceDao();
        }


        @Override
        protected Void doInBackground(final Void... params) {
            // Start the app with a clean database every time.
            // Not needed if you only populate on creation.

            mDao.deleteAll();
            Device d = new Device("","00:00:000:000",30);
            mDao.addDevice(d);
            return null;
        }
    }


}
