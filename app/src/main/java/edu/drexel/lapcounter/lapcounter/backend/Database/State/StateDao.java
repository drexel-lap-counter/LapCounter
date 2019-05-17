package edu.drexel.lapcounter.lapcounter.backend.Database.State;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

import edu.drexel.lapcounter.lapcounter.backend.Database.Units.Units;

/**
 * StateDAO for State objects to allow for interaction with the Database.
 * Given to TransitionRepo by Database in order for interactions to be done to the states_table.
 * Distributed to Repos by LapCounterDatabase
 * @see edu.drexel.lapcounter.lapcounter.backend.Database.LapCounterDatabase
 */
@Dao
public interface StateDao
{
    /**
     * Inserts given State into the database.
     * @param state State to be inserted.
     */
    @Insert()
    void insert(State state);

    /**
     * Returns all States in states_stable.
     * Will return Empty List if there are none in DB.
     * @return List of all states, or empty list
     */
    @Query("SELECT * FROM states_table")
    List<State> getAllStates();

    /**
     * Count the rows in the table. This is used to check whether the table is empty.
     * @return the number of rows in the states_table
     */
    @Query("SELECT COUNT(*) from states_table")
    int getNumRows();

    /**
     * Deletes given state from DB if it exists.
     * @param state State to be deleted.
     */
    @Delete()
    void deleteState(State state);
}
