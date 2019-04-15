package edu.drexel.lapcounter.lapcounter.backend;

import android.arch.persistence.room.TypeConverter;
import android.util.Log;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TimestampConverter
{
    private static final int MILLI_IN_SEC = 1000;
    @TypeConverter
    public static Date fromTimestamp(long value)
    {
        return new Date(value*MILLI_IN_SEC);
    }

    @TypeConverter
    public static long dateToTimestamp(Date date)
    {
        if(date == null)
        {
            return -1;
        }
        else
        {
            return date.getTime() / MILLI_IN_SEC;
        }
    }
}
