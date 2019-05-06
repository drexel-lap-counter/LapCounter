package edu.drexel.lapcounter.lapcounter.backend.Database.Workout;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.support.test.InstrumentationRegistry;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import edu.drexel.lapcounter.lapcounter.backend.Database.Device.Device;
import edu.drexel.lapcounter.lapcounter.backend.Database.Device.DeviceRepository;
import edu.drexel.lapcounter.lapcounter.backend.Database.Units.Units;
import edu.drexel.lapcounter.lapcounter.backend.TimestampConverter;
import kotlin.Unit;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

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

        workout_repo.deleteAll();
        workout_repo.deleteUnits(units);


    }

    @After
    public void cleanUp() throws Exception
    {
       workout_repo.deleteAll();
       workout_repo.deleteUnits(units);
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
        boolean val = retreived.equals(workout_one);
        assertTrue(val);
    }

}
