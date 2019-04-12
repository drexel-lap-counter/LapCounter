package edu.drexel.lapcounter.lapcounter.backend.Database.Device;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;

import java.util.ArrayList;

public class DeviceViewModel extends AndroidViewModel {

    private DeviceRepository mRepository;
    // Using LiveData and caching what getAlphabetizedWords returns has several benefits:
    // - We can put an observer on the data (instead of polling for changes) and only update the
    //   the UI when the data actually changes.
    // - Repository is completely separated from the UI through the ViewModel.
    private ArrayList<Device> mAllDevices;

    public DeviceViewModel(Application application) {
        super(application);
        mRepository = new DeviceRepository(application);
        mAllDevices = mRepository.getAllDevices();
    }

    public ArrayList<Device> getAllDevices() {
        return mAllDevices;
    }

    public void insert(Device device)
    {
        mRepository.insert(device);
    }

    public Device getDeviceByMacAddress(String mac)
    {
        return mRepository.getDeviceByMacAddress(mac);
    }

    public void deleteByMacAddress(String mac) {
        mRepository.deleteByMacAddress(mac);
    }
}
