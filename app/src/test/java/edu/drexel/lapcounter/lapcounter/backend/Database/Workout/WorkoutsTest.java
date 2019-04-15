package edu.drexel.lapcounter.lapcounter.backend.Database.Workout;

import org.junit.Before;
import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.*;

public class WorkoutsTest {

    Workouts workout;
    @Before
    public void setUp() throws Exception {
        workout = new Workouts();
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

        workout.setStartDateTime(date);
        Date retrieved = workout.getStartDateTime();

        assertEquals(date.getTime(), retrieved.getTime());
    }

    @Test
    public void getEndDateTime_retreives_setEndDateTime() {
        Date date = new Date();

        workout.setEndDateTime(date);
        Date retrieved = workout.getEndDateTime();

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
}