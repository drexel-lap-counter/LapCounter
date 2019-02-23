package edu.drexel.lapcounter.lapcounter.backend.Database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;



@Dao
public interface WorkoutDao {

    @Insert
    public void addWorkout(Workouts workout);

    @Query("SELECT * FROM workouts_table")
    public List<Workouts> getAllWorkouts();


    @Query("SELECT * FROM workouts_table")
    public Workouts firstWorkout();


//    @Query("SELECT * FROM user WHERE user_name LIKE :name AND last_name LIKE :last")
//    public abstract List<User> findUsersByNameAndLastName(String name, String last);
//

//
//    @Query("SELECT * FROM workouts_table WHERE Start_Date BETWEEN :item1Id AND :item2ID")
//    public abstract List<User> findUsersByNameAndLastNames(int item1Id, int item2ID);

//        public List<Workouts> numberofWorkouts();
    @Query("SELECT * FROM workouts_table WHERE Start_Date BETWEEN :item1Id AND :item2ID")
    public abstract List<Workouts> findWorkoutsBetweenDates(int item1Id, int item2ID);


    @Query("DELETE FROM workouts_table")
    void deleteAll();

    @Update
    public void updateWorkout(Workouts workout);


    @Delete
    public void deleteWorkout(Workouts workout);

}
