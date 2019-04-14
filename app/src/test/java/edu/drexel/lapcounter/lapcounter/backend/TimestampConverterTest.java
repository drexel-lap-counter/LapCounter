package edu.drexel.lapcounter.lapcounter.backend;

import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.*;

public class TimestampConverterTest {

    @Test
    public void fromTimestamp_returns_correct_date_if_valid() {
        String dateString = "2014-10-28 19:00:23.000";

        Date date = TimestampConverter.fromTimestamp(dateString);

        assertEquals(1414537223000l, date.getTime());
    }

    @Test
    public void fromTimestamp_returns_null_if_invalid() {
        String dateString = "2014-108 19:00:23.000";

        Date date = TimestampConverter.fromTimestamp(dateString);

        assertNull(date);
    }

    @Test
    public void fromTimestamp_returns_null_if_parameter_is_null() {
        String dateString = null;

        Date date = TimestampConverter.fromTimestamp(dateString);

        assertNull(date);
    }

    @Test
    public void dateToString_returns_string_if_date_is_not_null() {
        Date date = new Date();

        String string = TimestampConverter.dateToString(date);

        assertNotNull(string);
    }

    @Test
    public void dateToString_returns_null_if_date_is_null() {
        Date date = null;

        String string = TimestampConverter.dateToString(date);

        assertNull(string);
    }
}