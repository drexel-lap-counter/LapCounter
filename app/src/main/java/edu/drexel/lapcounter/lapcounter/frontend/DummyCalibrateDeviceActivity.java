package edu.drexel.lapcounter.lapcounter.frontend;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import edu.drexel.lapcounter.lapcounter.R;
import edu.drexel.lapcounter.lapcounter.backend.Database.Device.Device;
import edu.drexel.lapcounter.lapcounter.backend.Database.Device.DeviceViewModel;
import edu.drexel.lapcounter.lapcounter.frontend.navigationbar.NavBar;

/**
 * a calibration screen replacement for dummy devices and emulators.
 */
public class DummyCalibrateDeviceActivity extends AppCompatActivity {
    private final NavBar mNavBar = new NavBar(this);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dummy_calibrate_device);

        // In the final version, use R.string.<string id> for titles
        getSupportActionBar().setTitle("Calibrate Device");

        mNavBar.init();
    }

    /**
     * navigates to device selection screen.
     * @param view
     */
    public void finishCalibration(View view) {
        // TODO: Not sure if it's best to go back to the device selection or
        // device info page.

        //Added this code for testing purposes with emulator
        String mac_address = getIntent().getStringExtra(CalibrateDeviceActivity.EXTRAS_DEVICE_ADDRESS);
        String device_name = getIntent().getStringExtra(CalibrateDeviceActivity.EXTRAS_DEVICE_NAME);
        Device d = new Device(device_name,mac_address,-20);
        DeviceViewModel mDeviceViewModel = ViewModelProviders.of(this).get(DeviceViewModel.class);
        mDeviceViewModel.insert(d);

        Intent intent = new Intent(this, DeviceSelectActivity.class);
        startActivity(intent);
    }
}
