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

    @Delete()
    void deleteUnits(Units units);
}
