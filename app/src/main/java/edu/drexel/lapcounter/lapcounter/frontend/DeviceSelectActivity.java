package edu.drexel.lapcounter.lapcounter.frontend;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import edu.drexel.lapcounter.lapcounter.R;

/**
 * It's a bit confusing at this point, but this class is for scanning for *registered*
 * bluetooth devices. DeviceScanActivity is for scanning for *new devices*
 */
public class DeviceSelectActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_select);

        // In the final version, use R.string.<string id> for titles
        getSupportActionBar().setTitle("Select Device");
    }

    // NOTE: there should be a second action for selecting a device as the active one to use
    // for lap counting. However, that should not require transitioning to a new activity
    // so I omit it for now
    public void viewDevice(View view) {
        Intent intent = new Intent(this, DeviceInfoActivity.class);
        // TODO: use intent.putExtra() to set the device name/address
        startActivity(intent);
    }

    // This goes to DeviceScanActivity to select a new device to register
    public void scanForDevices(View view) {
        Intent intent = new Intent(this, DeviceScanActivity.class);
        startActivity(intent);
    }
}
