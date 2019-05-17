package edu.drexel.lapcounter.lapcounter.backend;

import android.arch.persistence.room.TypeConverter;
import android.util.Log;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * This class manages converting between Date objects and timestamps. This is used in the
 * Room database as a custom type converter
 */
public class TimestampConverter
{
    /**
     * There are 1000 ms in 1 sec
     */
    private static final int MILLI_IN_SEC = 1000;

    /**
     * Convert from a unix timestamp in seconds since epoch to a Date
     * @param value the Unix timestamp
     * @return a Date equivaelent of the timestamp
     */
    @TypeConverter
    public static Date fromTimestamp(long value)
    {
        return new Date(value*MILLI_IN_SEC);
    }

    /**
     * Convert from a Date to a Unix timestamp
     * @param date the input Date
     * @return a unix timestamp equivalent to this date and time.
     */
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
