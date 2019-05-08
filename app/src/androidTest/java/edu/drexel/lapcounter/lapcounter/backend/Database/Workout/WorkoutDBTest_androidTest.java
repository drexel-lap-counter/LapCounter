package edu.drexel.lapcounter.lapcounter.backend.Database.Workout;

import android.app.Application;
import android.support.test.InstrumentationRegistry;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import edu.drexel.lapcounter.lapcounter.backend.Database.Units.Units;
import edu.drexel.lapcounter.lapcounter.backend.TimestampConverter;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

public class WorkoutDBTest_androidTest {

    private Workout workout_one;
    private Workout workout_two;
    private Units units;
    private String TestUnits = "Test";
    private WorkoutRepository workout_repo;
    private long start_timestamp_one =1554904800; //03/10/2019 @ 10:00:00am utc
    private long end_timestamp_one = 1554912000; //03/11/2019 @ 12:00:00am utc
    private long start_timestamp_two = 1554976800; //03/15/2019 @ 2:00pm utc
    private long end_timestamp_two = 1554984000; //03/15/2019 @ 4:00pm utc
    private int wait_time = 100;


    @Before
    public void setUp() throws Exception
    {
        Application app = (Application) InstrumentationRegistry.getTargetContext().getApplicationContext();
        workout_repo = new WorkoutRepository(app);



        workout_one = new Workout();
        workout_one.setLaps(34);
        workout_one.setTotalDistanceTraveled(1200);
        workout_one.setPoolUnits(TestUnits);
        workout_one.setPoolLength(25);
        workout_one.setID(1);
        workout_one.setStartDate(TimestampConverter.fromTimestamp(start_timestamp_one));
        workout_one.setEndDate(TimestampConverter.fromTimestamp(end_timestamp_one));

        workout_two = new Workout();
        workout_two.setLaps(10);
        workout_two.setTotalDistanceTraveled(500);
        workout_two.setPoolUnits(TestUnits);
        workout_two.setPoolLength(50);
        workout_two.setID(2);
        workout_two.setStartDate(TimestampConverter.fromTimestamp(start_timestamp_two));
        workout_two.setEndDate(TimestampConverter.fromTimestamp(end_timestamp_two));

        units = new Units(TestUnits);

        workout_repo.deleteAllWorkouts();
        workout_repo.deleteUnits(units);


    }

    @After
    public void cleanUp() throws Exception
    {
       workout_repo.deleteAllWorkouts();
       workout_repo.deleteAllUnits();
    }


    @Test
    public void init_Units() throws Exception
    {
        final String yards = "Yards", meters = "Meters";
        workout_repo.initUnitsTable();
        Thread.sleep(wait_time);
        int count = workout_repo.getNumUnits();
        assertEquals(2,count);
        List<Units> all_units = workout_repo.getAllUnits();
        boolean have_yards = false, have_meters = false;
        for (Units u : all_units)
        {
            if(u.getUnitName().equals(yards))
            {
                have_yards = true;
            }

            if(u.getUnitName().equals(meters))
            {
                have_meters = true;
            }
        }
        assertTrue(have_yards);
        assertTrue(have_meters);
    }

    @Test
    public void insert_delete_Units() throws Exception
    {
        workout_repo.insert(units);
        Thread.sleep(wait_time);
        int num = workout_repo.getNumUnits();
        assertEquals(1,num);
        workout_repo.deleteUnits(units);
        Thread.sleep(wait_time);
        num = workout_repo.getNumUnits();
        assertEquals(0,num);

    }


    @Test
    public void insert_retrieve_Units() throws Exception
    {
        workout_repo.insert(units);
        Thread.sleep(wait_time);
        List<Units> retreived = workout_repo.getAllUnits();

        assertEquals(1,retreived.size());

        Units ret_units = retreived.get(0);
        assertTrue(ret_units.getUnitName().equals(TestUnits));
    }

    @Test
    public void insert_retrieve_Workout() throws Exception
    {
        workout_repo.insert(units);
        workout_repo.insert(workout_one);
        Thread.sleep(wait_time);
        int id = workout_one.getID();
        Workout retreived = workout_repo.getWorkoutByID(workout_one.getID());
        Thread.sleep(wait_time);
        boolean val = retreived.equals(workout_one);
        assertTrue(val);
    }

    @Test
    public void insert_delete_Workout() throws Exception
    {
        workout_repo.insert(units);
        workout_repo.insert(workout_one);
        Thread.sleep(wait_time);
        Workout retreived = workout_repo.getWorkoutByID(workout_one.getID());
        assertTrue(retreived != null && retreived.getID() == workout_one.getID());
        workout_repo.deleteWorkoutByID(workout_one.getID());
        Thread.sleep(wait_time);
        retreived = workout_repo.getWorkoutByID(workout_one.getID());
        assertTrue(retreived == null);
    }

    @Test
    public void insert_retreive_multiple_Workouts() throws Exception
    {
        workout_repo.insert(units);
        workout_repo.insert(workout_one);
        workout_repo.insert(workout_two);
        Thread.sleep(wait_time);
        List<Workout> retrieved = workout_repo.getAllWorkouts();
        assertEquals(2,retrieved.size());

    }
}
