package edu.drexel.lapcounter.lapcounter.backend;

import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.*;

public class TimestampConverterTest {

    @Test
    public void fromTimestamp_is_symmetrical_to_dateToTimestamp() {
        long timestamp = 1555418191l;

        Date date = TimestampConverter.fromTimestamp(timestamp);

        assertEquals(timestamp, TimestampConverter.dateToTimestamp(date));
    }

    @Test
    public void dateToTimestamp_returns_negativeOne_for_null_date() {
        Date date = null;

        long timestamp = TimestampConverter.dateToTimestamp(date);

        assertEquals(-1, timestamp);
    }
}