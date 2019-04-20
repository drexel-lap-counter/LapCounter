package edu.drexel.lapcounter.lapcounter.backend.Database.State;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

import edu.drexel.lapcounter.lapcounter.backend.Database.Units.Units;

@Dao
public interface StateDao
{
    @Insert()
    void insert(State state);

    @Query("SELECT * FROM states_table")
    List<State> getAllStates();

    /**
     * Count the rows in the table. This is used to check whether the table
     * is empty.
     * @return the number of rows in the units table
     */
    @Query("SELECT COUNT(*) from states_table")
    int getNumRows();

    @Delete()
    void deleteState(State state);
}
