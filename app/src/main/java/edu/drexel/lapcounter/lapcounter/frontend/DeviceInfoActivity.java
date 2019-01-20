package edu.drexel.lapcounter.lapcounter.frontend;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import edu.drexel.lapcounter.lapcounter.R;

public class DeviceInfoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_info);

        // In the final version, use R.string.<string id> for titles
        getSupportActionBar().setTitle("Device Info");
    }

    public void calibrate(View view) {
        Intent intent = new Intent(this, CalibrateDeviceActivity.class);
        startActivity(intent);
    }
}
