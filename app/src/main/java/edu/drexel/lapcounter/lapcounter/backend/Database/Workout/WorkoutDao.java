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


@Dao
public interface WorkoutDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void addWorkout(Workout workout);

    @Query("SELECT * FROM workouts")
    List<Workout> getAllWorkouts();

    @Query("SELECT * FROM workouts WHERE ID=:id")
    Workout getWorkoutByID(int id);

    @Query("DELETE FROM workouts")
    void deleteAll();

    @Query("DELETE FROM workouts WHERE ID=:id")
    void deleteWorkout(int id);

    @Query("SELECT * FROM workouts WHERE Start_Date>=:start_time AND End_Date<=:end_time")
    @TypeConverters({TimestampConverter.class})
    List<Workout> getWorkoutsByDateRange(Date start_time, Date end_time);

    @Query("SELECT * FROM workouts ORDER BY Start_Date DESC")
    List<Workout> getAllWorkoutsDecending();

}

