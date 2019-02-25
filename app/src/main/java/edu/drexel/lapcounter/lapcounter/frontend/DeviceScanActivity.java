package edu.drexel.lapcounter.lapcounter.frontend;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import edu.drexel.lapcounter.lapcounter.R;
import edu.drexel.lapcounter.lapcounter.backend.ble.DeviceScanner;
import edu.drexel.lapcounter.lapcounter.backend.ble.BLEScanner;
import edu.drexel.lapcounter.lapcounter.backend.dummy.DummyDeviceScanner;
import edu.drexel.lapcounter.lapcounter.frontend.navigationbar.NavBar;

public class DeviceScanActivity extends AppCompatActivity {

    private List<String> whitelist;
    private Button mButton;
    private Device mDevice;
    private RecyclerView mRecyclerView;
    private RecyclerAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private static final String TAG = DeviceScanActivity.class.getSimpleName();
    private final NavBar mNavBar = new NavBar(this);

    // Unique IDs for requesting permissions
    private static final int REQUEST_LOCATION = 1;
    private static final int REQUEST_ENABLE_BT = 2;

    private DeviceScanner mDeviceScanner;
    /**
     * This callback gets called *once per device discovered*. Use it to populate
     *
     * TODO: Evaluate ListView vs RecyclerView. ListView is apparently deprecated but simpler.
     */
    private DeviceScanner.Callback mDeviceCallback = new DeviceScanner.Callback() {
        @Override
        public void onDeviceFound(String deviceName, String deviceAddress, int rssi) {
            Log.i(TAG, String.format("Discovered '%s' '%s' %s", deviceName, deviceAddress, rssi));
            Device found_device = new Device(deviceName,deviceAddress,rssi);
            if(!whitelist.contains(found_device.getMac()))
            {
                mAdapter.addItem(found_device);
                mAdapter.notifyDataSetChanged();
            }
            // TODO: Store the device information and display it in the list however you need.
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_scan);

        // In the final version, use R.string.<string id> for titles
        getSupportActionBar().setTitle("Add New Device");

        mNavBar.init();

        //Once data storge implementation is complete, load whitelist from storage
        //It is either that, or get it from DeviceSelect, but i think loading again
        //might be better then passing all data to this activity,
        whitelist = new ArrayList<String>();
        whitelist.add("FF:FF:FF:FF:FF:00");
        whitelist.add("FF:FF:FF:FF:FF:01");
        mButton = findViewById(R.id.button13);
        //RecyclerView
        mRecyclerView = findViewById(R.id.device_scan_recycler_view);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
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
                TextView connected_view= findViewById(R.id.scan_device_selected);
                String device_name = (String) selected_view.getText();
                mButton.setAlpha(1);
                mButton.setEnabled(true);
                connected_view.setText(device_name);
                mDevice = mAdapter.getDevice(device_name);
            }

            @Override
            public void onLongClick(View view, int position) {
                //TODO: This is where you select which item we wish to connect to via bluetooth
                //Get the data needed from the view, and do bluetooth connection stuff
                //if we successfully connect, change text in the selected text box
                TextView selected_view = (TextView) view;
                Log.i(TAG,String.format("What was longclicked?: %s",selected_view.getText()));
                TextView connected_view= findViewById(R.id.scan_device_selected);
                String device_name = (String) selected_view.getText();
                mButton.setAlpha(1);
                mButton.setEnabled(true);
                connected_view.setText(device_name);
                mDevice = mAdapter.getDevice(device_name);
            }
        }));

        requestBluetoothPermission();
    }

    private boolean getLocationPermission() {
        String permission = Manifest.permission.ACCESS_COARSE_LOCATION;
        int currentPermission = ActivityCompat.checkSelfPermission(this, permission);
        boolean canRequest = currentPermission == PackageManager.PERMISSION_GRANTED;

        if (canRequest) {
            ActivityCompat.requestPermissions(this, new String[]{permission}, REQUEST_LOCATION);
            return true;
        }

        return false;
    }

    private void requestBluetoothPermission() {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        // User chose not to enable Bluetooth. Exit gracefully.
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK /*&& getLocationPermission()*/) {
                mDeviceScanner = new BLEScanner(this);
            } else {
                mDeviceScanner = new DummyDeviceScanner();
            }

            // Set a callback for whenever we find a bluetooth device
            mDeviceScanner.setCallback(mDeviceCallback);

            // NOTE: Unlike DeviceSelectActivity, we do NOT add a whitelist here.

            // Start the scan. This will call the callback a bunch of times.
            mDeviceScanner.startScan();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onPause()
    {
        super.onPause();

        if (mDeviceScanner != null) {
            mDeviceScanner.stopScan();
        }

        mAdapter.clearItems();
        mAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        if (mDeviceScanner != null) {
            mDeviceScanner.startScan();
        }
    }

    public void selectDevice(View view) {
        // TODO: Register the device before transitioning
        //Pass device object to intent
        // Go directly to calibration
        Intent intent = new Intent(this, DummyCalibrateDeviceActivity.class);
        intent.putExtra("DEVICE_NAME",mDevice.getName());
        intent.putExtra("DEVICE_MAC",mDevice.getMac());
        intent.putExtra("DEVICE_RSSI",mDevice.getRssi());
        startActivity(intent);
    }
}
