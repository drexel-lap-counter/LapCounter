package edu.drexel.lapcounter.lapcounter.backend.Database.State;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import edu.drexel.lapcounter.lapcounter.backend.lapcounter.LocationStateMachine;

@Entity(tableName = "states_table")
public class State
{
    /*** final static string for states ***/
    public static final String NEAR = LocationStateMachine.State.NEAR.toString();
    public static final String FAR = LocationStateMachine.State.FAR.toString();
    public static final String UNKNOWN = LocationStateMachine.State.UNKNOWN.toString();

    /** List of all values for easy DB initialization **/
    public static final String[] STATE_VALUES = new String[]{NEAR, FAR, UNKNOWN};

    @PrimaryKey()
    @NonNull
    @ColumnInfo(name="State_Name")
    private String mStateName = UNKNOWN;

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
