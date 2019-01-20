package edu.drexel.lapcounter.lapcounter.frontend;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import edu.drexel.lapcounter.lapcounter.R;
import edu.drexel.lapcounter.lapcounter.backend.BLEScanner;
import edu.drexel.lapcounter.lapcounter.backend.dummy.DummyDeviceScanner;

public class DeviceScanActivity extends AppCompatActivity {
    private static final String TAG = DeviceScanActivity.class.getSimpleName();

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
            Log.i(TAG, String.format("Discovered '%s' '%s' %s", deviceName, deviceAddress, rssi));

            // TODO: Store the device information and display it in the list however you need.
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_scan);

        // In the final version, use R.string.<string id> for titles
        getSupportActionBar().setTitle("Add New Device");

        // Set a callback for whenever we find a bluetooth device
        mDeviceScanner.setCallback(mDeviceCallback);

        // NOTE: Unlike DeviceSelectActivity, we do NOT add a whitelist here.

        // Start the scan. This will call the callback a bunch of times.
        mDeviceScanner.startScan();
    }

    public void selectDevice(View view) {
        // TODO: Register the device before transitioning

        // Go directly to calibration
        Intent intent = new Intent(this, CalibrateDeviceActivity.class);
        startActivity(intent);
    }
}
