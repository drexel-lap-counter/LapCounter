package edu.drexel.lapcounter.lapcounter.frontend;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import edu.drexel.lapcounter.lapcounter.R;
import edu.drexel.lapcounter.lapcounter.frontend.navigationbar.NavBar;

public class DeviceInfoActivity extends AppCompatActivity {
    private final NavBar mNavBar = new NavBar(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_info);

        // In the final version, use R.string.<string id> for titles
        getSupportActionBar().setTitle("Device Info");

        mNavBar.init();
    }

    public void calibrate(View view) {
        Intent intent = new Intent(this, CalibrateDeviceActivity.class);
        startActivity(intent);
    }
}
