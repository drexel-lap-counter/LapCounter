package edu.drexel.lapcounter.lapcounter.backend.Database.Units;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

/**
 * Units object used to represent the units a workout is measured in.
 * Tracks what measurement the pool was in for a given workout.
 * <p>
 * Class also used to interact with DB table units_table using UnitsDao.<p>
 * field of object:
 * Unit_Name: Name of Unit (Primary Key)
 * @see edu.drexel.lapcounter.lapcounter.backend.Database.Workout.Workout
 * @see edu.drexel.lapcounter.lapcounter.backend.Database.LapCounterDatabase
 * @see UnitsDao
 */
@Entity(tableName = "units_table")
public class Units
{
    /** Enum values */

    /**
     * String value for METERS.
     */
    public static final String METERS = "Meters";
    /**
     * String value for YARDS.
     */
    public static final String YARDS = "Yards";

    /**
     *  List of all enum values for easy database initialization.
     */
    public static final String[] UNITS_VALUES = new String[]{METERS, YARDS};

    /**
     * Name of the units.
     * Primary Key.
     */
    @PrimaryKey()
    @NonNull
    @ColumnInfo(name="Unit_Name")
    private String mUnitName;


    /**
     * Gets the Units name.
     * @return String of units name.
     */
    public String getUnitName()
    {
        return mUnitName;
    }

    /**
     * Sets a Units name.
     * @param units String value to set units name to.
     */
    public void setUnitName(String units)
    {
        this.mUnitName = units;
    }

    /**
     * Empty Constructor
     */
    public Units()
    {

    }


    /**
     * Constructor with all data fields entered.
     * @param name Name of Units.
     */
    public Units(String name)
    {
        this.mUnitName = name;
    }

}
