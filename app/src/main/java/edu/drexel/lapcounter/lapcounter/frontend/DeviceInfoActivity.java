package edu.drexel.lapcounter.lapcounter.frontend;

import android.content.Intent;
import android.nfc.Tag;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.util.Log;

import edu.drexel.lapcounter.lapcounter.R;
import edu.drexel.lapcounter.lapcounter.frontend.navigationbar.NavBar;

/**
 * this activity controls a screen that shows the details of a selected BLE device
 */
public class DeviceInfoActivity extends AppCompatActivity {
    private final NavBar mNavBar = new NavBar(this);

    /**
     * @param savedInstanceState
     * gets the intent from the preceding screen and sets the values for textViews
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_info);
        Intent intent = getIntent();
        String device_name = intent.getStringExtra("DEVICE_NAME");
        String device_mac = intent.getStringExtra("DEVICE_MAC");
        int device_rssi =  intent.getIntExtra("DEVICE_RSSI",0);

        TextView name_view = findViewById(R.id.name_text_view);
        name_view.setText(device_name);
        Log.i("TAG","Test1");

        TextView mac_view = findViewById(R.id.mac_text_view);
        mac_view.setText(device_mac);
        Log.i("TAG","Test2");

        TextView rssi_view = findViewById(R.id.rssi_text_view);
        rssi_view.setText(Integer.toString(device_rssi));
        Log.i("TAG","Test3");

        // In the final version, use R.string.<string id> for titles
        getSupportActionBar().setTitle("Device Info");

        mNavBar.init();
    }

    /**
     * @param view
     * button action for the calibrate button; navigates to calibration for the current device
     */
    public void calibrate(View view) {
        Intent intent = new Intent(this, CalibrateDeviceActivity.class);
        startActivity(intent);
    }
}
