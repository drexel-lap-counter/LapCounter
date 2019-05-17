package edu.drexel.lapcounter.lapcounter.backend.Database.Transition;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.TypeConverters;
import android.support.annotation.NonNull;

import java.util.Date;
import java.util.Locale;

import edu.drexel.lapcounter.lapcounter.backend.Database.State.State;
import edu.drexel.lapcounter.lapcounter.backend.Database.Workout.Workout;
import edu.drexel.lapcounter.lapcounter.backend.TimestampConverter;

import static android.arch.persistence.room.ForeignKey.CASCADE;
import static android.arch.persistence.room.ForeignKey.NO_ACTION;

/** Transition object used to represent a state switch in lap counting.
 * Tracks movement of the device by showing when the device changes states
 * <p>
 * Class also used to interact with DB table transitions_table using TransitionDao
 * fields of object:
 * Workout_ID       ID of the workout in DB that is associated with this transition
 * Transition_Time  (NonNull) Time that transition occured
 * Before_State:    State of device before transition
 * After_State:     State of device after transition
 *
 * Keys:
 * Primary Key: Composite Key of Workout_ID and Transition_Time
 * Foreign Key: Workout_ID from workouts, on delete is CASCADE
 *              Before_State from States, on delete is NO_ACTION;
 *              After_State from States, on delete is NO_ACTION;
 * @see edu.drexel.lapcounter.lapcounter.backend.Database.LapCounterDatabase
 * @see Workout
 * @see State
 * @see TransitionDao
 */
@Entity(tableName = "transitions_table",
        primaryKeys = {"Workout_ID", "Transition_Time"},
        foreignKeys = {
            @ForeignKey(
                    entity = Workout.class,
                    parentColumns = "ID",
                    childColumns = "Workout_ID",
                    onDelete = CASCADE),
            @ForeignKey(
                    entity = State.class,
                    parentColumns = "State_Name",
                    childColumns = "Before_State",
                    // states should never be deleted, so
                    // if a State gets deleted the database
                    // will reject the change because of this
                    // transition table constraint.
                    onDelete = NO_ACTION),
            @ForeignKey(
                    entity = State.class,
                    parentColumns = "State_Name",
                    childColumns = "After_State",
                    onDelete = NO_ACTION)
            })
public class Transition {
    /**
     * ID of workout this transition is associated with.
     */
    @ColumnInfo(name="Workout_ID")
    private int mWorkoutID;

    /**
     * Time that the transition occured.
     * TimeStampConverter class used to convert Java Date class to epoch timestamp
     * @see TimestampConverter
     */
    @ColumnInfo(name="Transition_Time")
    @TypeConverters({TimestampConverter.class})
    @NonNull
    private Date mTransitionTime;

    /**
     * State that the device was in before the transition.
     */
    @ColumnInfo(name="Before_State")
    private String mBeforeState;

    /**
     * State that the device was in after the transition.
     */
    @ColumnInfo(name="After_State")
    private String mAfterState;


    /**
     * Returns the WorkoutID of the transition
     * @return the workout id associated with transition
     */
    public int getWorkoutID() {
        return mWorkoutID;
    }

    /**
     * Sets workoutID to be given value
     * @param mWorkoutID int value of the associated workouts ID
     */
    public void setWorkoutID(int mWorkoutID) {
        this.mWorkoutID = mWorkoutID;
    }

    /**
     * returns the time the transition occured
     * @return time of transition
     */
    public Date getTransitionTime() {
        return mTransitionTime;
    }

    /**
     * Sets the time the transition occured.
     * @param mTransitionTime Date value of the transition time.
     */
    public void setTransitionTime(Date mTransitionTime) {
        this.mTransitionTime = mTransitionTime;
    }

    /**
     * Gets the State that the device was in before transition.
     * @return String name of the State.
     */
    public String getBeforeState() {
        return mBeforeState;
    }

    /**
     * Sets the State that the device was in before transition.
     * @param mBeforeState String name of the State
     */
    public void setBeforeState(String mBeforeState) {
        this.mBeforeState = mBeforeState;
    }

    /**
     * Gets the State that the device was in after transition.
     * @return String name of the State.
     */
    public String getAfterState() {
        return mAfterState;
    }

    /**
     * Sets the state that the device was in after transition.
     * @param mAfterState String name of the state.
     */
    public void setAfterState(String mAfterState) {
        this.mAfterState = mAfterState;
    }

    /**
     * Constructor of Transition with Time and States.
     * @param mTransitionTime Date object that is time when transition occured.
     * @param mBeforeState    String state name of state before transition.
     * @param mAfterState     String state naem of state after transition.
     */
    public Transition(Date mTransitionTime, String mBeforeState, String mAfterState) {
        this.mTransitionTime = mTransitionTime;
        this.mBeforeState = mBeforeState;
        this.mAfterState = mAfterState;
    }
    @Override
    public String toString() {
        return String.format(Locale.US, "Transition (%s, %d): %s -> %s",
                getTransitionTime().toString(), getWorkoutID(), getBeforeState(), getAfterState());
    }
}
