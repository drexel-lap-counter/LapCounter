package edu.drexel.lapcounter.lapcounter.backend.Database.Device;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.ArrayList;
import java.util.List;

import edu.drexel.lapcounter.lapcounter.backend.Database.Workout.Workouts;


@Dao
public interface DeviceDao {

    @Insert
    void addDevice(Device device);

    @Query("SELECT * FROM devices")
    List<Device> getAllDevices();

    @Query("SELECT * FROM devices WHERE mac_address=:mac")
    Device getDeviceByMacAddress(String mac);

    @Query("DELETE FROM devices")
    void deleteAll();

}
