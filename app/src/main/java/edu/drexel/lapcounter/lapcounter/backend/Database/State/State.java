package edu.drexel.lapcounter.lapcounter.backend.Database.State;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import edu.drexel.lapcounter.lapcounter.backend.lapcounter.LocationStateMachine;

/**
 * State object that is used to represent different states that a device can be in.
 * Three possible states, NEAR, FAR , UNKNOWN
 * NEAR: device is close to the phone (within threshold)
 * FAR: device is away from the phone (outside threshold)
 * UNKNOWN: devices location is unknown
 * <p>
 * This class is also used to interact with the Roomdatabase states_table.
 * Is a foreign key in the Transitions table<p>
 * has the following field<p>
 * mStateName: Name of the state. (Primary Key)
 * @see edu.drexel.lapcounter.lapcounter.backend.Database.Transition.Transition
 * @see edu.drexel.lapcounter.lapcounter.backend.Database.LapCounterDatabase
 */
@Entity(tableName = "states_table")
public class State
{
    /*** final static string for states ***/
    /**
     * String value for NEAR
     */
    public static final String NEAR = LocationStateMachine.State.NEAR.toString();
    /**
     * String value for FAR
     */
    public static final String FAR = LocationStateMachine.State.FAR.toString();
    /**
     * String value for UNKNOWN
     */
    public static final String UNKNOWN = LocationStateMachine.State.UNKNOWN.toString();

    /**
     * List of all values for easy DB initialization
     */
    public static final String[] STATE_VALUES = new String[]{NEAR, FAR, UNKNOWN};


    /**
     * Primary Key of State table State_Name.
     * Defaults to UNKNOWN.
     */
    @PrimaryKey()
    @NonNull
    @ColumnInfo(name="State_Name")
    private String mStateName = UNKNOWN;

    /**
     * Gets State's Name.
     * @return name of State
     */
    public String getStateName(){return mStateName;}

    /**
     * Sets name of state.
     * @param name new name for State.
     */
    public void setStateName(String name){this.mStateName = name;}

    /**
     * Empty Constructor.
     */
    public State()
    {

    }

    /**
     * Constructor for State with all data.
     * @param name name of the State.
     */
    public State(String name)
    {
        this.mStateName = name;
    }

}
