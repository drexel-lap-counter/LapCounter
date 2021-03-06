package edu.drexel.lapcounter.lapcounter.frontend;

import android.Manifest;
import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
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
 * lists bluetooth devices that are available and not currently registered.
 */
public class DeviceScanActivity extends AppCompatActivity {

    private List<String> whitelist;
    private Button mButton;
    private Device mDevice;
    private DeviceViewModel mDeviceViewModel;
    private RecyclerAdapter mAdapter;

    private static final String TAG = DeviceScanActivity.class.getSimpleName();
    private final NavBar mNavBar = new NavBar(this);

    // Unique IDs for requesting permissions
    private static final int REQUEST_LOCATION = 1;
    private static final int REQUEST_ENABLE_BT = 2;

    private DeviceScanner mDeviceScanner;
    /**
     * This callback gets called *once per device discovered*. Use it to populate
     */
    private DeviceScanner.Callback mDeviceCallback = new DeviceScanner.Callback() {
        @Override
        public void onDeviceFound(String deviceName, String deviceAddress, int rssi) {
            Log.i(TAG, String.format("Discovered '%s' '%s' %s", deviceName, deviceAddress, rssi));
            Device found_device = new Device(deviceName,deviceAddress,rssi);
            if(!whitelist.contains(found_device.getMacAddress()))
            {
                mAdapter.addItem(found_device);
                mAdapter.notifyDataSetChanged();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_scan);

        // In the final version, use R.string.<string id> for titles
        getSupportActionBar().setTitle("Add New Device");

        mNavBar.init();

        mDeviceViewModel = ViewModelProviders.of(this).get(DeviceViewModel.class);
        List<Device> registered_devices = mDeviceViewModel.getAllDevices();
        whitelist = new ArrayList<String>();
        for(Device device: registered_devices)
        {
            whitelist.add(device.getMacAddress());
        }
        mButton = findViewById(R.id.button13);

        initRecyclerView();
        requestBluetoothPermission();

    }

    private void initScanner(DeviceScanner scanner) {
        mDeviceScanner = scanner;
        mDeviceScanner.setCallback(mDeviceCallback);
        mDeviceScanner.startScan();
    }

    /**
     * Initalizes the Recycler view for listing all found devices.
     */
    private void initRecyclerView()
    {
        //RecyclerView
        RecyclerView mRecyclerView = findViewById(R.id.device_scan_recycler_view);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        ArrayList<Device> Dataset = new ArrayList<Device>();
        mAdapter = new RecyclerAdapter(Dataset,getResources().getColor(R.color.zebraStripeColorLight),getResources().getColor(R.color.zebraStripeColorDark));
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(getBaseContext(), mRecyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                TextView selected_view = (TextView) view;
                mAdapter.setSelectedPos(position);
                mAdapter.notifyDataSetChanged();
                Log.i(TAG,String.format("What was onclicked?: %s",selected_view.getText()));
                TextView connected_view= findViewById(R.id.scan_device_selected);
                String device_name = (String) selected_view.getText();
                mButton.setAlpha(1);
                mButton.setEnabled(true);
                connected_view.setText(device_name);
                mDevice = mAdapter.getDevice(device_name);
            }
        }));
    }


    private void initBleScanner() {
        initScanner(new BLEScanner(this));
    }

    private void initDummyScanner() {
        initScanner(new DummyDeviceScanner());
    }

    private void requestBluetoothPermission() {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                requestLocationPermission();
            } else {
                initDummyScanner();
            }
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void requestLocationPermission() {
        String permission = Manifest.permission.ACCESS_COARSE_LOCATION;
        boolean isAlreadyGranted = ActivityCompat.checkSelfPermission(this, permission) ==
                                   PackageManager.PERMISSION_GRANTED;

        if (isAlreadyGranted) {
            Log.i(TAG, "Location permission already granted.");
            initBleScanner();
            return;
        }

        Log.i(TAG, "Requesting location permission.");
        ActivityCompat.requestPermissions(this, new String[]{permission}, REQUEST_LOCATION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        if (requestCode != REQUEST_LOCATION) {
            return;
        }

        boolean didUserCancelRequest = grantResults.length == 0 ||
                grantResults[0] != PackageManager.PERMISSION_GRANTED;

        if (didUserCancelRequest) {
            Toast.makeText(this, R.string.request_location_rationale, Toast.LENGTH_LONG).show();
            initDummyScanner();
        } else {
            initBleScanner();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mDeviceScanner != null) {
            mDeviceScanner.stopScan();
        }

        mAdapter.clearItems();
        mAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mDeviceScanner != null) {
            mDeviceScanner.startScan();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mDeviceScanner != null) {
            mDeviceScanner.stopScan();
        }
    }

    /**
     * launches the calibrate device activity for a selected device
     * @param view
     */
    public void calibrateSelectedDevice(View view) {

        boolean shouldLaunchDummyCalibrate = mDeviceScanner instanceof DummyDeviceScanner;

        Class activityToLaunch = shouldLaunchDummyCalibrate ?
                                 DummyCalibrateDeviceActivity.class :
                                 CalibrateDeviceActivity.class;

        Intent intent = new Intent(this, activityToLaunch);
        intent.putExtra(CalibrateDeviceActivity.EXTRAS_DEVICE_ADDRESS, mDevice.getMacAddress());
        intent.putExtra(CalibrateDeviceActivity.EXTRAS_DEVICE_NAME, mDevice.getName());
        startActivity(intent);
    }
}
