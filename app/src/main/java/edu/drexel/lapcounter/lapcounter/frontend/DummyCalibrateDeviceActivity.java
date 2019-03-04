package edu.drexel.lapcounter.lapcounter.frontend;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import edu.drexel.lapcounter.lapcounter.R;
import edu.drexel.lapcounter.lapcounter.frontend.navigationbar.NavBar;

public class DummyCalibrateDeviceActivity extends AppCompatActivity {
    private final NavBar mNavBar = new NavBar(this);
    private final String mDeviceName = "DEVICE_NAME";
    private final String mDeviceMAC = "DEVICE_MAC";
    private final String mDeviceRSSI = "DEVICE_RSSI";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dummy_calibrate_device);

        // In the final version, use R.string.<string id> for titles
        getSupportActionBar().setTitle("Calibrate Device");

        mNavBar.init();
    }

    public void finishCalibration(View view) {
        // TODO: Not sure if it's best to go back to the device selection or
        // device info page.
        Intent intent = new Intent(this, DeviceSelectActivity.class);
        startActivity(intent);
    }
}