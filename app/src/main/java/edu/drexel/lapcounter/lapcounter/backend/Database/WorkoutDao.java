package edu.drexel.lapcounter.lapcounter.backend.Database;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.TypeConverters;
import android.arch.persistence.room.Update;

import java.util.Date;
import java.util.List;

import edu.drexel.lapcounter.lapcounter.backend.TimestampConverter;


@Dao
public interface WorkoutDao {

    @Insert
    void addWorkout(Workouts workout);

    @Query("SELECT * FROM workouts_table")
    LiveData<List<Workouts>> getAllWorkouts();

    @Query("SELECT * FROM workouts_table")
    List<Workouts> getAllWorkout();

    @Query("SELECT * FROM workouts_table WHERE ID=:id")
    Workouts getWorkoutByID(int id);

    @Query("DELETE FROM workouts_table")
    void deleteAll();

    @Query("SELECT * FROM workouts_table WHERE Start_DateTime BETWEEN :item1Id AND :item2ID ORDER BY Start_DateTime ASC")
    @TypeConverters({TimestampConverter.class})
    List<Workouts> findWorkoutsBetweenDates(Date item1Id, Date item2ID);

}


