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



}
