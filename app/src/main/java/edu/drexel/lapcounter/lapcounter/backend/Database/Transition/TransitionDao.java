package edu.drexel.lapcounter.lapcounter.backend.Database.Transition;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

/**
 * TransitionDao for interface with the RoomDatabase transition table transitions_table.
 * This serves as an interface for interacting with the DB specifically for Transition objects.
 * Distributed to Repos by LapCounterDatabase
 * @see edu.drexel.lapcounter.lapcounter.backend.Database.LapCounterDatabase
 */
@Dao
public interface TransitionDao {

    /**
     * Adds given transition to database, failing if a transition with same key already exists.
     * @param transition the transition to be added to db.
     */
    @Insert(onConflict = OnConflictStrategy.FAIL)
    void insertTransition(Transition transition);

    /**
     * deletes all transitions that are associated with a given workout id
     * @param id id of workout whose transitions will be deleted
     */
    @Query("DELETE FROM transitions_table WHERE Workout_ID=:id")
    void deleteByWorkout(int id);

    /**
     * Returns a list of transactions for a given workout ID.
     * @param id Id of workout whose transitions will be retrieved.
     * @return List of transitions associated with given workout id
     */
    @Query("SELECT * FROM transitions_table WHERE Workout_ID=:id ORDER BY Transition_Time")
    List<Transition> getTransitionsByWorkout(int id);

    /**
     * returns all transitions that are stored within the table.
     * @return  List of all transitions that exist with db table.
     */
    @Query("SELECT * FROM transitions_table ORDER BY Transition_Time")
    List<Transition> getAllTransitions();
}
