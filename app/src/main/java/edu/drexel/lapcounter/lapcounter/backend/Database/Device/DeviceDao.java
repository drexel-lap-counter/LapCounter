package edu.drexel.lapcounter.lapcounter.backend.Database.Device;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

/**
 * DeviceDAO is used for interface with the RoomDatabase device table name devices.
 * This serves as an interface for interacting with the DB specifically for device objects
 * Distributed to Repos by LapCounterDatabase
 * @see edu.drexel.lapcounter.lapcounter.backend.Database.LapCounterDatabase
 */
@Dao
public interface DeviceDao {

    /**
     * Adds given device to database, replacing if mac addresses are the name.
     * @param device the device to be added to db.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void addDevice(Device device);

    /**
     * Gets all devices stored in database
     * @return List of all device objects that are stored in DB
     */
    @Query("SELECT * FROM devices")
    List<Device> getAllDevices();

    /**
     * returns a device object if a row in DB has given mac address, else null.
     * Given a mac address as a string, will look to see if a device is stored with that mac.
     * If it is, it is returned, else null is returned.
     * @param mac  The mac address to search for.
     * @return Device object if mac address is found, else null
     */
    @Query("SELECT * FROM devices WHERE mac_address=:mac")
    Device getDeviceByMacAddress(String mac);

    /**
     * Deletes all rows from the devices table, removing all devices.
     */
    @Query("DELETE FROM devices")
    void deleteAll();

    /**
     * Deletes device from database if it has given mac address.
     * @param mac mac address to search for
     */
    @Query("DELETE FROM devices WHERE mac_address=:mac")
    void deleteByMacAddress(String mac);

}

