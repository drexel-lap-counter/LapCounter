package edu.drexel.lapcounter.lapcounter.backend.Database.Workout;

import android.app.Activity;
import android.app.Application;


import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import edu.drexel.lapcounter.lapcounter.backend.Database.Device.Device;
import edu.drexel.lapcounter.lapcounter.backend.Database.Device.DeviceDao;
import edu.drexel.lapcounter.lapcounter.backend.Database.Device.DeviceRepository;
import edu.drexel.lapcounter.lapcounter.backend.Database.Device.DeviceViewModel;
import edu.drexel.lapcounter.lapcounter.backend.Database.LapCounterDatabase;
import edu.drexel.lapcounter.lapcounter.backend.Database.Units.Units;

import edu.drexel.lapcounter.lapcounter.backend.TimestampConverter;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class WorkoutDBTest {
/*
    @Mock
    private Application app;

    @Mock
    private Context mockApplicationContext;
    @Mock
    private Resources mockContextResources;
    @Mock
    SharedPreferences mockSharedPreferences;

    Workout workout;
    WorkoutRepository workout_repo;
    DeviceRepository device_repo;
    long start_timestamp_one =1552212000; //03/10/2019 @ 10:00:00am utc
    long end_timestamp_one = 1552219200; //03/11/2019 @ 12:00:00am utc
    long start_timestamp_two = 1552658400; //03/15/2019 @ 2:00pm utc
    long end_timestamp_two = 1552665600; //03/15/2019 @ 4:00pm utc
    @Before
    public void setUp() throws Exception
    {

        MockitoAnnotations.initMocks(this);
        when(app.getApplicationContext()).thenReturn(mockApplicationContext);
        //ViewModelProviders.
        //Application app = (Application) mockApplicationContext;
        workout_repo = new WorkoutRepository(app);
        device_repo = new DeviceRepository(app);
        List<Units> t = workout_repo.getAllUnits();
        List<Workout> w = workout_repo.getAllWorkouts();
        List<Device> d = device_repo.getAllDevices();
        //db = Room.inMemoryDatabaseBuilder(InstrumentationRegistry.getContext(),LapCounterDatabase.class).build();
        //workoutDao = db.workoutDao();
        //unitsDao = db.unitsDao();
        //deviceDao = db.deviceDao();

        Device dd = new Device();
        dd.setMacAddress("Test:MAC");
        dd.setThreshold(-50);
        dd.setName("Test Name");
        device_repo.insert(dd);
        workout = new Workout();
        workout.setLaps(20);
        workout.setDeviceMAC("Test:MAC");
        workout.setTotalDistanceTraveled(500);
        workout.setPoolUnits("Meters");
        workout.setPoolLength(25);
        workout.setID(1);
        workout.setStartDate(TimestampConverter.fromTimestamp(start_timestamp_one));
        workout.setEndDate(TimestampConverter.fromTimestamp(end_timestamp_one));
        workout_repo.insert(new Units("Meters"));
        workout_repo.insert(new Units("Yards"));

        int a = 1;
        workout_repo.insert(workout);
    }

    @Test
    public void getWorkoutById_retrieves_insert()
    {
        int id =1;
        Workout retrieved = workout_repo.getWorkoutByID(id);
        assertEquals(workout,retrieved);
    }
*/
}
