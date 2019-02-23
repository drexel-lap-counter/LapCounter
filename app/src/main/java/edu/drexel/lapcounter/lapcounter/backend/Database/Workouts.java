package edu.drexel.lapcounter.lapcounter.backend.Database;


import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity(tableName = "workouts_table")
public class Workouts {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name ="Start_Date")
    public int mStartDate;

    @ColumnInfo(name ="Start_Time")
    public int mStartTime;

    @ColumnInfo(name ="End_Date")
    public int mEndDate;

    @ColumnInfo(name ="End_Time")
    public int mEndTime;

    @ColumnInfo(name = "Pool_length")
    public int mPoolLength;

    @ColumnInfo(name = "Total_Distance_Traveled")
    public int mTotalDistanceTraveled;

    @ColumnInfo(name = "Average Workout Distance")
    public int mAvgWorkoutDistance;


    //
    //GETTERS
    //


    public int getID(){
        return id;
    }

    public int getStartDate() {
        return mStartDate;
    }


    public int getStartTime() {
        return mStartTime;
    }

    public int getEndDate() {
        return mEndDate;
    }

    public int getEndTime() {
        return mEndTime;
    }

    public int getTotalDistanceTraveled(){
        return mTotalDistanceTraveled;
    }

    public int getPoolLength(){
        return mPoolLength;
    }

    public int getAvgWorkoutDistance(){
        return mAvgWorkoutDistance;
    }


    //
    //SETTERS
    //
    public void setID(int id){
        this.id = id;
    }

    public void setmStartDate(int startDate) {
        this.mStartDate = startDate;
    }


    public void setStartTime(int startTime) {
        this.mStartTime = startTime;
    }


    public void setEndDate(int endDate) {
        this.mEndDate = endDate;
    }

    public void setEndTime(int endTime) {
        this.mEndTime = endTime;
    }

    public void setTotalDistanceTraveled(int totalDistanceTraveled){
        this.mTotalDistanceTraveled = totalDistanceTraveled;
    }

    public void setPoolLength(int poolLength){
        this.mPoolLength = poolLength;
    }

    public void setAvgWorkoutDistance(int avgWorkoutDistance){
        this.mAvgWorkoutDistance = avgWorkoutDistance;
    }

}
