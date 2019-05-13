package edu.drexel.lapcounter.lapcounter.backend.Database.Units;

import android.app.Application;
import android.support.test.InstrumentationRegistry;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import edu.drexel.lapcounter.lapcounter.backend.Database.Workout.WorkoutRepository;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

public class UnitsDBTest
{
    private WorkoutRepository workout_repo;
    private Units units;
    private String TestUnits = "Test";
    private int wait_time = 300;

    @Before
    public void SetUp() throws Exception
    {
        Application app = (Application) InstrumentationRegistry.getTargetContext().getApplicationContext();
        workout_repo = new WorkoutRepository(app);

        units = new Units(TestUnits);

        workout_repo.deleteAllWorkouts();
        workout_repo.deleteUnits(units);
        Thread.sleep(wait_time);
    }

    @After
    public void cleanUp() throws Exception
    {
        workout_repo.deleteAllWorkouts();
        workout_repo.deleteAllUnits();
        Thread.sleep(wait_time);
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

}
