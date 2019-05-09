package edu.drexel.lapcounter.lapcounter.frontend;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;

import edu.drexel.lapcounter.lapcounter.R;
import edu.drexel.lapcounter.lapcounter.backend.Database.Device.Device;
import edu.drexel.lapcounter.lapcounter.backend.Database.Device.DeviceViewModel;
import edu.drexel.lapcounter.lapcounter.backend.ble.BLEScanner;
import edu.drexel.lapcounter.lapcounter.backend.ble.DeviceScanner;
import edu.drexel.lapcounter.lapcounter.backend.dummy.DummyDeviceScanner;
import edu.drexel.lapcounter.lapcounter.frontend.navigationbar.NavBar;

/**
 * It's a bit confusing at this point, but this class is for scanning for *registered*
 * bluetooth devices. DeviceScanActivity is for scanning for *new devices*
 */
public class DeviceSelectActivity extends AppCompatActivity {

    private Button mInfoButton;
    private Device mDevice;
    private RecyclerView mRecyclerView;
    private RecyclerAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private DeviceViewModel mDeviceViewModel;
    private final NavBar mNavBar = new NavBar(this);

    private static final String TAG = DeviceSelectActivity.class.getSimpleName();

    public static final String PREFS_KEY = "lapcounter_device_selection";
    public static final String KEY_DEVICE_ADDRESS = "device_address";

    // Sample device scanner
    private DeviceScanner mDeviceScanner = new BLEScanner(this);//new DummyDeviceScanner();

    /**
     * This callback gets called *once per device discovered*. Use it to populate
     *
     * TODO: Evaluate ListView vs RecyclerView. ListView is apparently deprecated but simpler.
     */
    private DeviceScanner.Callback mDeviceCallback = new DeviceScanner.Callback() {
        @Override
        public void onDeviceFound(String deviceName, String deviceAddress, int rssi) {
            Log.i(TAG, String.format("Found Registered Device '%s' '%s' %s", deviceName, deviceAddress, rssi));
            Device found_device = mDeviceViewModel.getDeviceByMacAddress(deviceAddress);
            mAdapter.addItem(found_device);
            if(mDevice != null && found_device.getName().equals(mDevice.getName()))
                mAdapter.setSelectedPos(mAdapter.getPosition(found_device.getName()));
            mAdapter.notifyDataSetChanged();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_select);

        getSupportActionBar().setTitle(R.string.select_device);

        mNavBar.init();

        mInfoButton = findViewById(R.id.button9);

        //RecyclerView
        mRecyclerView = findViewById(R.id.device_select_recycler_view);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mDeviceViewModel = ViewModelProviders.of(this).get(DeviceViewModel.class);
        ArrayList<Device> Dataset = new ArrayList<Device>();
        mAdapter = new RecyclerAdapter(Dataset,getResources().getColor(R.color.zebraStripeColorLight),getResources().getColor(R.color.zebraStripeColorDark));
        mRecyclerView.setAdapter(mAdapter);

        mRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(getBaseContext(), mRecyclerView, new RecyclerTouchListener.ClickListener() {

            @Override
            public void onClick(View view, int position) {
                // Get the selected device
                TextView selected_view = (TextView) view;
                String device_name = (String) selected_view.getText();
                mDevice = mAdapter.getDevice(device_name);
                mAdapter.setSelectedPos(position);
                mAdapter.notifyDataSetChanged();

                // Select this device
                selectDevice(mDevice);

                // Update the screen
                refreshUI(mDevice);
            }
        }));

        checkForSelectedDevice();

        // Set a callback for whenever we find a bluetooth device
        mDeviceScanner.setCallback(mDeviceCallback);

        // Will need to store this info somewhere
        // Example of setting a whitelist. This is so only registered devices show up.
        List<String> whitelist = new ArrayList<>();
        List<Device> registered_devices = mDeviceViewModel.getAllDevices();
        for(Device d : registered_devices)
        {
            whitelist.add(d.getMacAddress());
        }

        mDeviceScanner.setAddressWhitelist(whitelist);

        // Start the scan. This will call the callback a bunch of times.
        mDeviceScanner.startScan();
    }


    @Override
    protected void onPause()
    {
        super.onPause();
        mDeviceScanner.stopScan();
        mAdapter.clearItems();
        mAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        mDeviceScanner.startScan();
    }

    public void viewDevice(View view) {
        Intent intent = new Intent(this, DeviceInfoActivity.class);
        intent.putExtra(DeviceInfoActivity.EXTRAS_DEVICE_NAME, mDevice.getName());
        intent.putExtra(DeviceInfoActivity.EXTRAS_DEVICE_ADDRESS, mDevice.getMacAddress());
        intent.putExtra(
                DeviceInfoActivity.EXTRAS_USE_DUMMY_CALIBRATOR,
                mDeviceScanner instanceof DummyDeviceScanner);
        startActivity(intent);
    }

    // This goes to DeviceScanActivity to select a new device to register
    public void scanForDevices(View view) {
        Intent intent = new Intent(this, DeviceScanActivity.class);
        startActivity(intent);
    }

    private void selectDevice(Device device) {
        SharedPreferences prefs = getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_DEVICE_ADDRESS, device.getMacAddress());
        editor.apply();
    }

    private void checkForSelectedDevice() {
        mDevice = fetchSelectedDevice();
        if (mDevice != null) {
            refreshUI(mDevice);
        }
    }

    private Device fetchSelectedDevice() {
        SharedPreferences prefs = getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE);
        String address = prefs.getString(KEY_DEVICE_ADDRESS, null);

        if (address != null) {
            DeviceViewModel dvm = ViewModelProviders.of(this).get(DeviceViewModel.class);
            return dvm.getDeviceByMacAddress(address);
        } else {
            return null;
        }
    }

    private void refreshUI(Device device) {
        TextView connected_view = findViewById(R.id.connected_device_view);
        connected_view.setText(device.getName());

        mInfoButton.setAlpha(1);
        mInfoButton.setEnabled(true);
    }
}

