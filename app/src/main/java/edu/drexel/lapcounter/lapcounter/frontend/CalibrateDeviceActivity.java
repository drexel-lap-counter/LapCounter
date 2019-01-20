package edu.drexel.lapcounter.lapcounter.frontend;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import edu.drexel.lapcounter.lapcounter.R;

public class CalibrateDeviceActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calibrate_device);

        // In the final version, use R.string.<string id> for titles
        getSupportActionBar().setTitle("Calibrate Device");
    }

    public void finishCalibration(View view) {
        // TODO: Not sure if it's best to go back to the device selection or
        // device info page.
        Intent intent = new Intent(this, DeviceSelectActivity.class);
        startActivity(intent);
    }
}
