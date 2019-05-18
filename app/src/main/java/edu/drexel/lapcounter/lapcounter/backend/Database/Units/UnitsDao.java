package edu.drexel.lapcounter.lapcounter.backend.Database.Units;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

/**
 * UnitsDAO is used for interface with the Room Units table units_table.
 * This serves as an interface for interacting with the DB specifically for Units objects
 * Distributed to Repos by LapCounterDatabase
 * @see edu.drexel.lapcounter.lapcounter.backend.Database.LapCounterDatabase
 */
@Dao
public interface UnitsDao
{
    /**
     * Adds given Units to database.
     * @param units the units to be added to db.
     */
    @Insert()
    void insert(Units units);

    /**
     * Gets list of all Units currently in the database.
     * @return List of all Units in database.
     */
    @Query("SELECT * FROM units_table")
    List<Units> getAllUnits();

    /**
     * Count the rows in the table. This is used to check whether the table is empty.
     * @return the number of rows in the units table
     */
    @Query("SELECT COUNT(*) from units_table")
    int getNumRows();

    /**
     * Deletes given Units from table.
     * @param units Units to be deleted from db.
     */
    @Delete()
    void deleteUnits(Units units);

    /**
     * Deletes all units from database.
     */
    @Query("DELETE FROM units_table")
    void deleteAllUnits();
}
