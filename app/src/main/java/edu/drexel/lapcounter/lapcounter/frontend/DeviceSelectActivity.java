package edu.drexel.lapcounter.lapcounter.frontend;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
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
import edu.drexel.lapcounter.lapcounter.backend.Database.Workout.WorkoutViewModel;
import edu.drexel.lapcounter.lapcounter.backend.ble.DeviceScanner;
import edu.drexel.lapcounter.lapcounter.backend.dummy.DummyDeviceScanner;
import edu.drexel.lapcounter.lapcounter.frontend.navigationbar.NavBar;

/**
 * It's a bit confusing at this point, but this class is for scanning for *registered*
 * bluetooth devices. DeviceScanActivity is for scanning for *new devices*
 */
public class DeviceSelectActivity extends AppCompatActivity {

    //TODO: Finalize Device Code.  This will be used with on clicks to create a device object
    //that can be passed to view device OR we get that from the bluetooth adapter/settings itself
    private Button mInfoButton;
    private Device mDevice;
    private RecyclerView mRecyclerView;
    private RecyclerAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private DeviceViewModel mDeviceViewModel;
    private final NavBar mNavBar = new NavBar(this);

    private static final String TAG = DeviceSelectActivity.class.getSimpleName();

    // Sample device scanner
    private DeviceScanner mDeviceScanner = new DummyDeviceScanner();

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
            mAdapter.notifyDataSetChanged();
            // TODO: Store the device information and display it in the list however you need.
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
        ArrayList<Device> myDataset = new ArrayList<Device>();
        mAdapter = new RecyclerAdapter(myDataset);
        mRecyclerView.setAdapter(mAdapter);

        mRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(getBaseContext(), mRecyclerView, new RecyclerTouchListener.ClickListener() {

            @Override
            public void onClick(View view, int position) {
                    //TODO: This is where you select which item we wish to connect to via bluetooth
                    //Get the data needed from the view, and do bluetooth connection stuff
                    //if we successfully connect, change text in the selected text box
                    TextView selected_view = (TextView) view;
                    Log.i(TAG,String.format("What was onclicked?: %s",selected_view.getText()));
                    TextView connected_view= findViewById(R.id.connected_device_view);
                    String device_name = (String) selected_view.getText();
                    connected_view.setText(device_name);
                    mDevice = mAdapter.getDevice(device_name);
                    mInfoButton.setAlpha(1);
                    mInfoButton.setEnabled(true);

                    // TODO: Replace with the appropriate post-device selection Activity.
            }

            @Override
            public void onLongClick(View view, int position) {
                onClick(view, position);
            }

        }));
        // Set a callback for whenever we find a bluetooth device
        mDeviceScanner.setCallback(mDeviceCallback);

        // TODO: Remove this example eventually
        // Will need to store this info somewhere
        // Example of setting a whitelist. This is so only registered devices show up.
        List<String> whitelist = new ArrayList<>();
        ArrayList<Device> registered_devices = mDeviceViewModel.getAllDevices();
        for(Device d : registered_devices)
        {
            whitelist.add(d.getMacAddress());
        }
        //whitelist.add("FF:FF:FF:FF:FF:00"); // Dummy A
        //whitelist.add("FF:FF:FF:FF:FF:01"); // Dummy B

        mDeviceScanner.setAddressWhitelist(whitelist);

        // Start the scan. This will call the callback a bunch of times.
        mDeviceScanner.startScan();


        // NOTE: I did not implement the onPause() and onResume() logic. Ideally you should
        // stop the scan on pause and start it on resume in both this aand DeviceScanActivity.
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



    // NOTE: there should be a second action for selecting a device as the active one to use
    // for lap counting. However, that should not require transitioning to a new activity
    // so I omit it for now
    public void viewDevice(View view) {
        Intent intent = new Intent(this, DeviceInfoActivity.class);
        // TODO: use intent.putExtra() to pass information to the next activity
        intent.putExtra("DEVICE_NAME",mDevice.getName());
        intent.putExtra("DEVICE_MAC",mDevice.getMacAddress());
        intent.putExtra("DEVICE_RSSI",mDevice.getThreshold());
        // See the prototype app for an example of this.
        startActivity(intent);
    }

    // This goes to DeviceScanActivity to select a new device to register
    public void scanForDevices(View view) {
        Intent intent = new Intent(this, DeviceScanActivity.class);
        startActivity(intent);
    }
}

