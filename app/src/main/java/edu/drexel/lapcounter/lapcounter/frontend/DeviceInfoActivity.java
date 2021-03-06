package edu.drexel.lapcounter.lapcounter.frontend;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.util.Log;

import edu.drexel.lapcounter.lapcounter.R;
import edu.drexel.lapcounter.lapcounter.backend.Database.Device.DeviceViewModel;
import edu.drexel.lapcounter.lapcounter.frontend.navigationbar.NavBar;

/**
 * Activity that lists the info about the currently selected device.  Allows user to re-calibrate or
 * delete the device from the database.
 */
public class DeviceInfoActivity extends AppCompatActivity {
    private final NavBar mNavBar = new NavBar(this);
    private String mDeviceName;
    private String mDeviceAddress;

    private static String qualify(String s) {
        return DeviceInfoActivity.class.getPackage().getName() + "." + s;
    }

    /**
     * labels the device address in an intent
     */
    public static final String EXTRAS_DEVICE_ADDRESS = qualify("DEVICE_ADDRESS");
    /**
     * labels the device name in an intent
     */
    public static final String EXTRAS_DEVICE_NAME = qualify("DEVICE_NAME");
    /**
     * marks the use of a dummy device
     */
    public static final String EXTRAS_USE_DUMMY_CALIBRATOR = qualify("USE_DUMMY_CALIBRRATOR");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_info);
        Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

        TextView name_view = findViewById(R.id.name_text_view);
        name_view.setText(mDeviceName);

        TextView mac_view = findViewById(R.id.mac_text_view);
        mac_view.setText(mDeviceAddress);

        // In the final version, use R.string.<string id> for titles
        getSupportActionBar().setTitle("Device Info");

        mNavBar.init();
    }

    /**
     * launches the CalibrateDeviceActivity when calibrate is selected.
     * @param view
     */
    public void calibrate(View view) {
        Intent parentIntent = getIntent();
        boolean useDummy = parentIntent.getBooleanExtra(EXTRAS_USE_DUMMY_CALIBRATOR, true);
        Class activityToLaunch = useDummy ?
                DummyCalibrateDeviceActivity.class :
                CalibrateDeviceActivity.class;


        Intent intent = new Intent(this, activityToLaunch);
        intent.putExtra(CalibrateDeviceActivity.EXTRAS_DEVICE_ADDRESS, mDeviceAddress);
        intent.putExtra(CalibrateDeviceActivity.EXTRAS_DEVICE_NAME, mDeviceName);
        startActivity(intent);
    }

    /**
     * provides a confirmation dialog before deleting device info.
     * @param view
     */
    public void confirmDelete(View view) {
        new AlertDialog.Builder(this)
                .setMessage("Are you sure you want to delete this device?")
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteDevice();
                    }
                })
                .setNegativeButton(R.string.no, null)
                .show();
    }


    /**
     * deletes the selected device and opens the DeviceSelectionActivity
     */
    public void deleteDevice() {
        // Delete the current device by mac address
        DeviceViewModel dvm = ViewModelProviders.of(this).get(DeviceViewModel.class);
        dvm.deleteByMacAddress(mDeviceAddress);

        // Clear the selected device from shared preferences
        SharedPreferences prefs = getSharedPreferences(
                DeviceSelectActivity.PREFS_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(DeviceSelectActivity.KEY_DEVICE_ADDRESS);
        editor.apply();

        // Go to Device Selection activity.
        Intent intent = new Intent(this, DeviceSelectActivity.class);
        startActivity(intent);
    }
}
