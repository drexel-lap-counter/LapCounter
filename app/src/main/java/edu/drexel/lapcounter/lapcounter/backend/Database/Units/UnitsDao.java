package edu.drexel.lapcounter.lapcounter.backend.Database.Units;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface UnitsDao
{
    @Insert()
    void insert(Units units);

    @Query("SELECT * FROM units_table")
    List<Units> getAllUnits();

    /**
     * Count the rows in the table. This is used to check whether the table
     * is empty.
     * @return the number of rows in the units table
     */
    @Query("SELECT COUNT(*) from units_table")
    int getNumRows();

    @Delete()
    void deleteUnits(Units units);
}
