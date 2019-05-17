package edu.drexel.lapcounter.lapcounter.backend.Database.Device;

import android.app.Application;
import android.os.AsyncTask;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import edu.drexel.lapcounter.lapcounter.backend.Database.LapCounterDatabase;

/**
 * DeviceRepository class for interacting with deviceDAO and the database
 * If you are in a service, use this to interact with db.
 * If you are in an activity use the DeviceViewModel instead.
 * Allows caller to asynchronously touch database using possible queries specifed in DeviceDao
 * @see DeviceDao
 * @see DeviceViewModel
 * @see Device
 * @see LapCounterDatabase
 */
public class DeviceRepository {

    /**
     * The device dao used for interacting with DB.
     */
    private DeviceDao mDeviceDao;


    /**
     * Constructor for DeviceRepository.
     * gets the database, and sets its device dao using DB.
     * @param application Application to get database from
     */
    public DeviceRepository(Application application) {
        LapCounterDatabase db = LapCounterDatabase.getDatabase(application);
        mDeviceDao = db.deviceDao();
    }

    /**
     * Gets all of the devices currently stored in database
     * @return List of all devices in DB.  Will have none if there is none in DB.
     */
    public List<Device> getAllDevices()
    {
        ExecutorService ex = Executors.newSingleThreadExecutor();
        Future<List<Device>> res = ex.submit(new Callable<List<Device>>() {
            @Override
            public List<Device> call() throws Exception {

                return mDeviceDao.getAllDevices();
            }
        });
        List<Device> devices = new ArrayList<Device>();
        try
        {
            devices = res.get();
        }
        catch(Exception e)
        {
            Log.i("ERROR",e.toString());
        }
        ArrayList<Device> output = new ArrayList<Device>();
        output.addAll(devices);
        return output;

    }

    /**
     * Inserts given device into the database.  Or updates if mac address already exists
     * @param device device to insert or update
     */
    public void insert(Device device)
    {
        new insertAsyncTask(mDeviceDao).execute(device);
    }

    /**
     * ASyncTask used for device insertion.
     * Allows for DB usage off of UI thread.
     */
    private static class insertAsyncTask extends AsyncTask<Device,Void,Void>
    {
        private DeviceDao mAsyncTaskDao;

        insertAsyncTask(DeviceDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Device... params) {
            mAsyncTaskDao.addDevice(params[0]);
            return null;
        }
    }

    /**
     * Gets the device in database with given mac address if it exists.
     * If it does not exist, it will return null.
     * @param mac mac address to look for.
     * @return Device of object with mac address if it is found, else null.
     */
    public Device getDeviceByMacAddress(final String mac)
    {
        ExecutorService ex = Executors.newSingleThreadExecutor();
        Future<Device> res = ex.submit(new Callable<Device>() {
            @Override
            public Device call() throws Exception {

                return mDeviceDao.getDeviceByMacAddress(mac);
            }
        });
        Device device = new Device();
        try
        {
            device = res.get();
        }
        catch(Exception e)
        {
            Log.i("ERROR",e.toString());
        }
        return device;

    }

    /**
     * Delete row in DB with given mac address if it exists.
     * @param mac mac address to delete from DB.
     */
    public void deleteByMacAddress(String mac) {
        new DeleteAsyncTask(mDeviceDao).execute(mac);
    }

    /**
     * ASyncTask used for device deletion by mac.
     * Allows for DB usage off of UI thread.
     */
    private static class DeleteAsyncTask extends AsyncTask<String, Void, Void> {
        private DeviceDao mAsyncTaskDao;

        DeleteAsyncTask(DeviceDao dao) {
            mAsyncTaskDao = dao;
        }


        @Override
        protected Void doInBackground(String... strings) {
            mAsyncTaskDao.deleteByMacAddress(strings[0]);
            return null;
        }
    }

    /**
     * Deletes all devices from the devices table in DB
     */
    public void deleteAllDevices() {
        new DeleteAllAsyncTask(mDeviceDao).execute();
    }

    /**
     * ASyncTask used for deletion of all devices.
     * Allows for DB usage off of UI thread.
     */
    private static class DeleteAllAsyncTask extends AsyncTask<Void, Void, Void> {
        private DeviceDao mAsyncTaskDao;

        DeleteAllAsyncTask(DeviceDao dao) {
            mAsyncTaskDao = dao;
        }


        @Override
        protected Void doInBackground(Void... params) {
            mAsyncTaskDao.deleteAll();
            return null;
        }
    }

}
