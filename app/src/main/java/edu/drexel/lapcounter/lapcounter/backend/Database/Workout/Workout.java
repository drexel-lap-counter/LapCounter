package edu.drexel.lapcounter.lapcounter.backend.Database.Workout;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.TypeConverters;
import android.support.annotation.NonNull;
import java.util.Date;
import edu.drexel.lapcounter.lapcounter.backend.Database.Device.Device;
import edu.drexel.lapcounter.lapcounter.backend.Database.Units.Units;
import edu.drexel.lapcounter.lapcounter.backend.TimestampConverter;
import static android.arch.persistence.room.ForeignKey.SET_NULL;

@Entity(tableName = "workouts",
        foreignKeys = {@ForeignKey(entity= Device.class,
                                   parentColumns = "mac_address",
                                    childColumns = "Device_Mac",
                                    onDelete = SET_NULL),
                       @ForeignKey(entity = Units.class,
                                    parentColumns = "Unit_Name",
                                    childColumns = "Pool_Units",
                                    onDelete = SET_NULL)})
public class Workout {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name ="Start_Date")
    @NonNull
    @TypeConverters({TimestampConverter.class})
    private Date mStartDate;

    @ColumnInfo(name ="End_Date")
    @TypeConverters({TimestampConverter.class})
    private Date mEndDate;

    @ColumnInfo(name = "Pool_length")
    private int mPoolLength;

    @ColumnInfo(name = "Total_Distance_Traveled")
    private int mTotalDistanceTraveled;

    @ColumnInfo(name="Pool_Units")
    private String mPoolUnits;

    @ColumnInfo(name = "Laps")
    private int mLaps;

    @ColumnInfo(name="Device_Mac")
    private String mDeviceMAC;

    //
    //GETTERS
    //


    public int getID(){
        return id;
    }

    public Date getStartDate()
    {
        return mStartDate;
    }

    public Date getEndDate() {
        return mEndDate;
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
    public String getDeviceMAC()
    {
        return mDeviceMAC;
    }


    //
    //SETTERS
    //
    public void setID(int id){
        this.id = id;
    }

    public void setStartDate(Date startDateTime) {
        this.mStartDate = startDateTime;
    }

    public void setEndDate(Date endDateTime) {
        this.mEndDate = endDateTime;
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
    public void setDeviceMAC(String device_mac)
    {
        this.mDeviceMAC = device_mac;
    }

    @Override
    public boolean equals(Object o)
    {
        if(o == this)
        {
            return true;
        }

        if(!(o instanceof Workout))
        {
            return false;
        }
        Workout w = (Workout) o;
        return ((w.getDeviceMAC()==null && this.mDeviceMAC==null) || w.getDeviceMAC().equals(this.mDeviceMAC)) &&
                w.getPoolUnits().equals(this.mPoolUnits) &&
                w.getEndDate().equals(this.mEndDate) &&
                w.getStartDate().equals(this.mStartDate) &&
                w.getID() == this.getID() &&
                w.getLaps() == this.getLaps() &&
                w.getPoolLength() == this.getPoolLength() &&
                w.getTotalDistanceTraveled() == this.getTotalDistanceTraveled();
    }


}
