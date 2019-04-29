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
    @ColumnInfo(name="Workout_ID")
    private int mWorkoutID;

    @ColumnInfo(name="Transition_Time")
    @TypeConverters({TimestampConverter.class})
    @NonNull
    private Date mTransitionTime;

    @ColumnInfo(name="Before_State")
    private String mBeforeState;

    @ColumnInfo(name="After_State")
    private String mAfterState;


    public int getWorkoutID() {
        return mWorkoutID;
    }

    public void setWorkoutID(int mWorkoutID) {
        this.mWorkoutID = mWorkoutID;
    }

    public Date getTransitionTime() {
        return mTransitionTime;
    }

    public void setTransitionTime(Date mTransitionTime) {
        this.mTransitionTime = mTransitionTime;
    }

    public String getBeforeState() {
        return mBeforeState;
    }

    public void setBeforeState(String mBeforeState) {
        this.mBeforeState = mBeforeState;
    }

    public String getAfterState() {
        return mAfterState;
    }

    public void setAfterState(String mAfterState) {
        this.mAfterState = mAfterState;
    }

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
