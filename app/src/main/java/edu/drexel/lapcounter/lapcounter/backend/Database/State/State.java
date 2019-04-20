package edu.drexel.lapcounter.lapcounter.backend.Database.State;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity(tableName = "states_table")
public class State
{
    /*** final static string for states ***/
    public static final String NEAR = "NEAR";
    public static final String FAR = "FAR";
    public static final String UNKNOWN = "UNKNOWN";

    /** List of all values for easy DB initialization **/
    public static final String[] STATE_VALUES = new String[]{NEAR, FAR, UNKNOWN};

    @PrimaryKey()
    @NonNull
    @ColumnInfo(name="State_Name")
    private String mStateName;

    public String getStateName(){return mStateName;}
    public void setStateName(String name){this.mStateName = name;}

    public State()
    {

    }

    public State(String name)
    {
        this.mStateName = name;
    }

}
