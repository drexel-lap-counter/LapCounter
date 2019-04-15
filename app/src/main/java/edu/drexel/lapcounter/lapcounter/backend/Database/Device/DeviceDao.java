package edu.drexel.lapcounter.lapcounter.backend.Database.Device;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;


@Dao
public interface DeviceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void addDevice(Device device);

    @Query("SELECT * FROM devices")
    List<Device> getAllDevices();

    @Query("SELECT * FROM devices WHERE mac_address=:mac")
    Device getDeviceByMacAddress(String mac);

    @Query("DELETE FROM devices")
    void deleteAll();

    @Query("DELETE FROM devices WHERE mac_address=:mac")
    void deleteByMacAddress(String mac);

}

