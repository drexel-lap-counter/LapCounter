package edu.drexel.lapcounter.lapcounter.backend.Database.Workout;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.TypeConverters;

import java.util.Date;

import edu.drexel.lapcounter.lapcounter.backend.TimestampConverter;

@Entity(tableName = "workouts_table")
public class Workouts {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name ="Start_DateTime")
    @TypeConverters({TimestampConverter.class})
    public Date mStartDateTime;

    @ColumnInfo(name ="End_DateTime")
    @TypeConverters({TimestampConverter.class})
    public Date mEndDateTime;

    @ColumnInfo(name = "Pool_length")
    public int mPoolLength;

    @ColumnInfo(name = "Total_Distance_Traveled")
    public int mTotalDistanceTraveled;

    @ColumnInfo(name="Pool_Units")
    public String mPoolUnits;

    @ColumnInfo(name = "Laps")
    public int mLaps;


    //
    //GETTERS
    //


    public int getID(){
        return id;
    }

    public Date getStartDateTime() {
        return mStartDateTime;
    }

    public Date getEndDateTime() {
        return mEndDateTime;
    }

    public int getTotalDistanceTraveled(){
        return mTotalDistanceTraveled;
    }

    public int getPoolLength(){
        return mPoolLength;
    }

    public String getPoolUnits()
    {
        return mPoolUnits;
    }
    public int getLaps()
    {
        return mLaps;
    }


    //
    //SETTERS
    //
    public void setID(int id){
        this.id = id;
    }

    public void setStartDateTime(Date startDateTime) {
        this.mStartDateTime = startDateTime;
    }

    public void setEndDateTime(Date endDateTime) {
        this.mEndDateTime = endDateTime;
    }

    public void setTotalDistanceTraveled(int totalDistanceTraveled){
        this.mTotalDistanceTraveled = totalDistanceTraveled;
    }

    public void setPoolLength(int poolLength){
        this.mPoolLength = poolLength;
    }

    public void setPoolUnits(String poolUnits)
    {
        this.mPoolUnits = poolUnits;
    }
    public void setLaps(int laps)
    {
        this.mLaps = laps;
    }

}
