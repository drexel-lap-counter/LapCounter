package edu.drexel.lapcounter.lapcounter.frontend;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import edu.drexel.lapcounter.lapcounter.R;

public class DeviceScanActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_scan);

        // In the final version, use R.string.<string id> for titles
        getSupportActionBar().setTitle("Add New Device");
    }

    public void selectDevice(View view) {
        // TODO: Register the device before transitioning

        // Go directly to calibration
        Intent intent = new Intent(this, CalibrateDeviceActivity.class);
        startActivity(intent);
    }
}
