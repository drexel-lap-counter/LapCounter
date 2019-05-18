package edu.drexel.lapcounter.lapcounter.backend.Database.Workout;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.TypeConverter;
import android.arch.persistence.room.TypeConverters;

import java.util.Date;
import java.util.List;

import edu.drexel.lapcounter.lapcounter.backend.TimestampConverter;

/**
 * WorkoutDAO used for interfacing with the RoomDatabase workouts table name: workouts
 * This serves as an interface for interacting with the DB specifically for device objects
 * Distributed to Repos by LapCounterDatabase
 * @see edu.drexel.lapcounter.lapcounter.backend.Database.LapCounterDatabase
 */
@Dao
public interface WorkoutDao {

    /**
     * Adds given workout to database, replacing if ids are the same.
     * @param workout the workout to be added or updated in db.
     * @return ID value of workout that was entered.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long addWorkout(Workout workout);

    /**
     * Returns a list of all workouts that exist in database.
     * Returns a list of Workout objects for all objects in DB, list will be empty if no data in db.
     * @return List of all Workout objects in database
     */
    @Query("SELECT * FROM workouts")
    List<Workout> getAllWorkouts();

    /**
     * Returns a workout from the database if it shares same id as parameter.
     * If the param id does not exist in db, null is returned.
     * @param id int id of workout to look for
     * @return Workout if found, else null.
     */
    @Query("SELECT * FROM workouts WHERE ID=:id")
    Workout getWorkoutByID(int id);

    /**
     * Deletes all Workouts from table.
     * Clears the database of all workouts
     */
    @Query("DELETE FROM workouts")
    void deleteAllWorkouts();

    /**
     * Clears a specific workout from the database, specified by id.
     * If a workout does not exist with param id, nothing happens to db.
     * @param id int id of workout to delete.
     */
    @Query("DELETE FROM workouts WHERE ID=:id")
    void deleteWorkout(int id);

    /**
     * Gets all workouts that are on or inbetween the given date range.
     * Returns a list of Workouts within date range, or empty list if there is none.
     * Uses type converter to convert from Date object to unix timestamp.
     * @param start_time Date object of start date and time to look for
     * @param end_time   Date object of end date and time to look for
     * @return List of Workouts that exist within range, or empty list if there is none.
     * @see TimestampConverter
     */
    @Query("SELECT * FROM workouts WHERE Start_Date>=:start_time AND End_Date<=:end_time")
    @TypeConverters({TimestampConverter.class})
    List<Workout> getWorkoutsByDateRange(Date start_time, Date end_time);

    /**
     * Gets all workouts within the data in descending order based on start date.
     * Returns a list of all workouts in desecnding order, or empty list if there is none.
     * @return List of all Workouts descending by start date, or empty list.
     */
    @Query("SELECT * FROM workouts ORDER BY Start_Date DESC")
    List<Workout> getAllWorkoutsDecending();

}

