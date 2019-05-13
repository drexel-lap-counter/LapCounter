package edu.drexel.lapcounter.lapcounter.backend.Database.Device;

import android.app.Application;
import android.support.test.InstrumentationRegistry;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import edu.drexel.lapcounter.lapcounter.backend.Database.Transition.TransitionRepository;
import edu.drexel.lapcounter.lapcounter.backend.Database.Workout.WorkoutRepository;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

public class DeviceDBTest
{
    private DeviceRepository device_repo;
    private Device device_one,device_two;
    private String device_one_mac = "00:00:00:00", device_two_mac = "00:00:00:01";
    private String device_one_name = "Test Device 1", device_two_name = "Test Device 2";
    private double device_one_threshold = -20, device_two_threshold = -35;

    private int wait_time = 300;



    @Before
    public void setUp() throws Exception
    {
        Application app = (Application) InstrumentationRegistry.getTargetContext().getApplicationContext();
        device_repo = new DeviceRepository(app);

        device_one = new Device();
        device_one.setMacAddress(device_one_mac);
        device_one.setName(device_one_name);
        device_one.setThreshold(device_one_threshold);

        device_two = new Device();
        device_two.setMacAddress(device_two_mac);
        device_two.setName(device_two_name);
        device_two.setThreshold(device_two_threshold);

        device_repo.deleteAllDevices();
        Thread.sleep(wait_time);

    }

    @After
    public void cleanUp() throws Exception
    {
        device_repo.deleteAllDevices();
        Thread.sleep(wait_time);
    }

    @Test
    public void insert_retrieve_Devices() throws Exception
    {
        List<Device> retrieved = device_repo.getAllDevices();
        assertEquals(0,retrieved.size());
        device_repo.insert(device_one);
        device_repo.insert(device_two);
        Thread.sleep(wait_time);
        retrieved = device_repo.getAllDevices();
        assertEquals(2,retrieved.size());
    }
    @Test
    public void insert_retrieve_Device() throws Exception
    {
        List<Device> retrieved = device_repo.getAllDevices();
        assertEquals(0,retrieved.size());
        device_repo.insert(device_one);
        Thread.sleep(wait_time);
        Device output = device_repo.getDeviceByMacAddress(device_one.getMacAddress());
        assertTrue(device_one.equals(output));
    }



    @Test
    public void insert_delete_Device() throws Exception
    {
        List<Device> retrieved = device_repo.getAllDevices();
        assertEquals(0,retrieved.size());
        device_repo.insert(device_one);
        Thread.sleep(wait_time);
        Device output = device_repo.getDeviceByMacAddress(device_one.getMacAddress());
        assertTrue(device_one.equals(output));
        device_repo.deleteByMacAddress(device_one.getMacAddress());
        Thread.sleep(wait_time);
        retrieved = device_repo.getAllDevices();
        assertEquals(0,retrieved.size());
    }

}



