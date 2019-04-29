package edu.drexel.lapcounter.lapcounter.backend.Database.Transition;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface TransitionDao {
    @Insert(onConflict = OnConflictStrategy.FAIL)
    void insertTransition(Transition transition);

    @Query("DELETE FROM transitions_table WHERE Workout_ID=:id")
    void deleteByWorkout(int id);

    @Query("SELECT * FROM transitions_table WHERE Workout_ID=:id ORDER BY Transition_Time")
    List<Transition> getTransitionsByWorkout(int id);

    @Query("SELECT * FROM transitions_table ORDER BY Transition_Time")
    List<Transition> getAllTransitions();
}
