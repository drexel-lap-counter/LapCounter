package edu.drexel.lapcounter.lapcounter.backend.Database.Workout;

import org.junit.Before;
import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.*;

public class WorkoutTest {

    Workout workout;
    @Before
    public void setUp() throws Exception {
        workout = new Workout();
    }

    @Test
    public void getID_retrieves_setId() {
        int id = 324;

        workout.setID(id);
        int retrieved = workout.getID();

        assertEquals(id, retrieved);
    }

    @Test
    public void getStartDateTime_retreives_setStartDateTime() {
        Date date = new Date();

        workout.setStartDate(date);
        Date retrieved = workout.getStartDate();

        assertEquals(date.getTime(), retrieved.getTime());
    }

    @Test
    public void getEndDateTime_retreives_setEndDateTime() {
        Date date = new Date();

        workout.setEndDate(date);
        Date retrieved = workout.getEndDate();

        assertEquals(date.getTime(), retrieved.getTime());
    }

    @Test
    public void getTotalDistanceTraveled_retrieves_setTotalDistanceTraveled() {
        int distance = 1000;

        workout.setTotalDistanceTraveled(distance);
        int retrieved = workout.getTotalDistanceTraveled();

        assertEquals(distance, retrieved);
    }

    @Test
    public void getPoolLength_retrieves_setPoolLength() {
        int length = 50;

        workout.setPoolLength(length);
        int retrieved = workout.getPoolLength();

        assertEquals(length, retrieved);
    }

    @Test
    public void getPoolUnits_retrieves_setPoolUnits() {
        String poolUnits = "YARDS";

        workout.setPoolUnits(poolUnits);
        String retrieved = workout.getPoolUnits();

        assertEquals(poolUnits, retrieved);
    }

    @Test
    public void getLaps_retrieves_setLaps() {
        int laps = 100;

        workout.setLaps(laps);
        int retrieved = workout.getLaps();

        assertEquals(laps, retrieved);
    }

    @Test
    public void getDeviceMAC_retrieves_setDeviceMAC() {
        String MAC = "12:f3:21:04:23";

        workout.setDeviceMAC(MAC);
        String retrieved = workout.getDeviceMAC();

        assertEquals(MAC, retrieved);
    }
}