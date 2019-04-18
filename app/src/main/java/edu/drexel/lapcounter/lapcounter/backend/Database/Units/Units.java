package edu.drexel.lapcounter.lapcounter.backend.Database.Units;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity(tableName = "units_table")
public class Units
{
    @PrimaryKey()
    @NonNull
    @ColumnInfo(name="Unit_Name")
    private String mUnitName;


    public String getUnitName()
    {
        return mUnitName;
    }

    public void setUnitName(String units)
    {
        this.mUnitName = units;
    }

    public Units()
    {

    }


    public Units(String name)
    {
        this.mUnitName = name;
    }

}
