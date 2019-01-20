package edu.drexel.lapcounter.lapcounter.frontend;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import edu.drexel.lapcounter.lapcounter.R;
import edu.drexel.lapcounter.lapcounter.backend.BLEScanner;
import edu.drexel.lapcounter.lapcounter.backend.dummy.DummyDeviceScanner;

/**
 * It's a bit confusing at this point, but this class is for scanning for *registered*
 * bluetooth devices. DeviceScanActivity is for scanning for *new devices*
 */
public class DeviceSelectActivity extends AppCompatActivity {
    private static final String TAG = DeviceSelectActivity.class.getSimpleName();

    // Sample device scanner
    private BLEScanner mDeviceScanner = new DummyDeviceScanner();

    /**
     * This callback gets called *once per device discovered*. Use it to populate
     *
     * TODO: Evaluate ListView vs RecyclerView. ListView is apparently deprecated but simpler.
     */
    private BLEScanner.Callback mDeviceCallback = new BLEScanner.Callback() {
        @Override
        public void onDeviceFound(String deviceName, String deviceAddress, int rssi) {
            Log.i(TAG, String.format("Found Registered Device '%s' '%s' %s", deviceName, deviceAddress, rssi));

            // TODO: Store the device information and display it in the list however you need.
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_select);

        // In the final version, use R.string.<string id> for titles
        getSupportActionBar().setTitle("Select Device");

        // Set a callback for whenever we find a bluetooth device
        mDeviceScanner.setCallback(mDeviceCallback);

        // TODO: Remove this example eventually
        // Example of setting a whitelist. This is so only registered devices show up.
        List<String> whitelist = new ArrayList<>();
        whitelist.add("FF:FF:FF:FF:FF:00"); // Dummy A
        whitelist.add("FF:FF:FF:FF:FF:01"); // Dummy B
        mDeviceScanner.setAddressWhitelist(whitelist);

        // Start the scan. This will call the callback a bunch of times.
        mDeviceScanner.startScan();

        // NOTE: I did not implement the onPause() and onResume() logic. Ideally you should
        // stop the scan on pause and start it on resume in both this aand DeviceScanActivity.
    }

    // NOTE: there should be a second action for selecting a device as the active one to use
    // for lap counting. However, that should not require transitioning to a new activity
    // so I omit it for now
    public void viewDevice(View view) {
        Intent intent = new Intent(this, DeviceInfoActivity.class);
        // TODO: use intent.putExtra() to pass information to the next activity
        // See the prototype app for an example of this.
        startActivity(intent);
    }

    // This goes to DeviceScanActivity to select a new device to register
    public void scanForDevices(View view) {
        Intent intent = new Intent(this, DeviceScanActivity.class);
        startActivity(intent);
    }
}
