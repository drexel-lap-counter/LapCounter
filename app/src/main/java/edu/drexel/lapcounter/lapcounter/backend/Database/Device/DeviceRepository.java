package edu.drexel.lapcounter.lapcounter.backend.Database.Device;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;
import android.util.Log;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import edu.drexel.lapcounter.lapcounter.backend.Database.Workout.WorkoutDao;
import edu.drexel.lapcounter.lapcounter.backend.Database.Workout.WorkoutDatabase;
import edu.drexel.lapcounter.lapcounter.backend.Database.Workout.Workouts;

public class DeviceRepository {

    private DeviceDao mDeviceDao;


    DeviceRepository(Application application) {
       DeviceDatabase db = DeviceDatabase.getDatabase(application);
        mDeviceDao = db.deviceDao();
    }

    ArrayList<Device> getAllDevices()
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

    public void insert(Device device)
    {
        new insertAsyncTask(mDeviceDao).execute(device);
    }

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

}
