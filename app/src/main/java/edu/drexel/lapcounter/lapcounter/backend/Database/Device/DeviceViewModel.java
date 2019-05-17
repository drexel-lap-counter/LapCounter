package edu.drexel.lapcounter.lapcounter.backend.Database.Device;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * ViewModel for Devices that are used by UI Activities to interact with the DB.
 * Has Repo object within it that is used to interacts with DAO that interacts with DB.
 * @see DeviceRepository
 * @see DeviceDao
 * @see Device
 * @see edu.drexel.lapcounter.lapcounter.backend.Database.LapCounterDatabase
 */
public class DeviceViewModel extends AndroidViewModel {

    /**
     * DeviceRepository used for interacting with DB's DeviceDao
     */
    private DeviceRepository mRepository;
    private List<Device> mAllDevices;

    /**
     * Constructor for setting up a DeviceViewModel
     * @param application Application to set up ViewModel for
     */
    public DeviceViewModel(Application application) {
        super(application);
        mRepository = new DeviceRepository(application);
        mAllDevices = mRepository.getAllDevices();
    }

    /**
     * Gets List of all devices currently within the DB
     * Returns Empty List if DB is empty
     * @return List of all devices in DB
     */
    public List<Device> getAllDevices() {
        return mAllDevices;
    }

    /**
     * Inserts device into database if it is not already in, else it updates it.
     * @param device Device to insert or update
     */
    public void insert(Device device)
    {
        mRepository.insert(device);
    }

    /**
     * Returns a Device if its mac address is equal to given, else null.
     * @param mac mac address of device to look for in DB
     * @return Device if found or null if not found.
     */
    public Device getDeviceByMacAddress(String mac)
    {
        return mRepository.getDeviceByMacAddress(mac);
    }

    /**
     * Deletes device for DB if it has same mac address as given.
     * @param mac mac addreess of device to delete from DB
     */
    public void deleteByMacAddress(String mac) {
        mRepository.deleteByMacAddress(mac);
    }

    /**
     * Deletes all devices from database.
     */
    public void deleteAllDevices()
    {
        mRepository.deleteAllDevices();
    }

}
