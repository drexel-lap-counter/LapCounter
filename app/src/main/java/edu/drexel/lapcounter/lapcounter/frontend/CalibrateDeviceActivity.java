package edu.drexel.lapcounter.lapcounter.frontend;

import android.annotation.SuppressLint;
import android.arch.lifecycle.ViewModelProviders;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import edu.drexel.lapcounter.lapcounter.R;
import edu.drexel.lapcounter.lapcounter.backend.Database.Device.Device;
import edu.drexel.lapcounter.lapcounter.backend.Database.Device.DeviceViewModel;
import edu.drexel.lapcounter.lapcounter.backend.Hyperparameters;
import edu.drexel.lapcounter.lapcounter.backend.ble.CalibrationRewardFunc;
import edu.drexel.lapcounter.lapcounter.backend.ble.RSSIManager;
import edu.drexel.lapcounter.lapcounter.backend.ble.RssiCollector;
import edu.drexel.lapcounter.lapcounter.backend.SimpleMessageReceiver;
import edu.drexel.lapcounter.lapcounter.backend.ble.BLEComm;
import edu.drexel.lapcounter.lapcounter.backend.ble.BLEService;

/**
 * Used to calibrate a device's RSSI threshold.
 */
public class CalibrateDeviceActivity extends AppCompatActivity {
    private final static String TAG = CalibrateDeviceActivity.class.getSimpleName();

    private static String qualify(String s) {
        return CalibrateDeviceActivity.class.getPackage().getName() + "." + s;
    }

    /**
     * labels the device address in an intent
     */
    public static final String EXTRAS_DEVICE_ADDRESS = qualify("DEVICE_ADDRESS");
    /**
     * labels the device name in an intent
     */
    public static final String EXTRAS_DEVICE_NAME = qualify("DEVICE_NAME");

    private final static int PRINT_COLLECTOR_STATS_FREQ_MS = 500;

    private TextView mCalibrateInfo;
    private Button mCalibrate;

    private BLEService mBleService;
    private String mDeviceAddress;
    private String mDeviceName;

    private final RssiCollector mRssiCollector = new RssiCollector();
    private long mLastPrintMillis;

    private final SimpleMessageReceiver mReceiver = new SimpleMessageReceiver();

    private final ServiceConnection mBleServiceConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            BLEService.LocalBinder binder = (BLEService.LocalBinder)service;
            mBleService = binder.getService();
            mBleService.connectToDevice(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBleService = null;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calibrate_device);

        mCalibrateInfo = findViewById(R.id.calibrate_info);
        mCalibrate = findViewById(R.id.calibrate);

        Intent intent = getIntent();
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mCalibrateInfo.setText(getString(R.string.label_connecting, mDeviceAddress));

        Intent bleServiceIntent = new Intent(this, BLEService.class);
        bindService(bleServiceIntent, mBleServiceConn, BIND_AUTO_CREATE);

        registerHandlers();
    }

    private void registerHandlers() {
        mReceiver.registerHandler(BLEComm.ACTION_CONNECTED, onConnect);
        mReceiver.registerHandler(BLEComm.ACTION_RECONNECTED, onConnect);
        mReceiver.registerHandler(BLEComm.ACTION_DISCONNECTED, onDisconnect);
        mReceiver.registerHandler(RSSIManager.ACTION_RSSI_AND_DIR_AVAILABLE, onRssi);
    }


    private final SimpleMessageReceiver.MessageHandler onConnect = new SimpleMessageReceiver.MessageHandler() {
        @Override
        public void onMessage(Intent message) {
            mCalibrateInfo.setText(R.string.label_calibrate_instructions);
            mCalibrate.setText(R.string.label_calibrate);
            mCalibrate.setEnabled(true);
        }
    };

    private final SimpleMessageReceiver.MessageHandler onDisconnect = new SimpleMessageReceiver.MessageHandler() {
        @Override
        public void onMessage(Intent message) {
            mRssiCollector.disable();
            mRssiCollector.clear();
            mCalibrateInfo.setText(R.string.label_device_disconnected_try_reconnect);
            mBleService.connectToDevice(mDeviceAddress);
            mCalibrate.setEnabled(false);
        }
    };

    private final SimpleMessageReceiver.MessageHandler onRssi = new SimpleMessageReceiver.MessageHandler() {
        @Override
        public void onMessage(Intent message) {
            double rssi = message.getDoubleExtra(RSSIManager.EXTRA_RSSI, 0.0);

            if (rssi == 0.0 || !mRssiCollector.isEnabled()) {
                return;
            }

            mRssiCollector.collect((int)rssi);

            long currentMillis = System.currentTimeMillis();
            long timeSinceLastPrint = currentMillis - mLastPrintMillis;

            if (timeSinceLastPrint >= PRINT_COLLECTOR_STATS_FREQ_MS) {
                CalibrationRewardFunc rewardFunc = Hyperparameters.CALIBRATION_REWARD_FUNC;
                String collectorState = mRssiCollector.toString(rewardFunc);
                mCalibrateInfo.setText(collectorState);
                mLastPrintMillis = currentMillis;
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        mReceiver.attach(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mReceiver.detach(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mReceiver.detach(this);
        mBleService.disconnectDevice();
        unbindService(mBleServiceConn);
        mBleService = null;
    }

    /**
     * starts and stops the calibration process
     * @param view
     */
    public void calibrate(View view) {
        if (mRssiCollector.isEnabled()) {
            finishCalibration();
            goToDeviceSelectScreen();
        } else {
            startCalibration();
        }
    }

    private void startCalibration() {
        mBleService.startRssiRequests();
        mRssiCollector.enable();
        mCalibrate.setText(getString(R.string.label_done));
    }

    private void finishCalibration() {
        mBleService.stopRssiRequests();
        mRssiCollector.disable();
    }

    private void goToDeviceSelectScreen() {
        // The reward function can be selected in the Hyperparameters class.
        CalibrationRewardFunc rewardFunc = Hyperparameters.CALIBRATION_REWARD_FUNC;

        double threshold = mRssiCollector.computeThreshold(rewardFunc);
        log_thread("Threshold %.3f", threshold);

        saveDevice(threshold);

        // Go back to the device selection activity.
        Intent goToDeviceSelect = new Intent(this, DeviceSelectActivity.class);
        startActivity(goToDeviceSelect);
        finish();
    }

    /**
     * Store the calibrated device in the database
     * @param threshold the calibrated threshold.
     */
    private void saveDevice(double threshold) {
        Device device = new Device(mDeviceName, mDeviceAddress, threshold);

        DeviceViewModel dvm = ViewModelProviders.of(this).get(DeviceViewModel.class);
        dvm.insert(device);
    }

    @SuppressLint("DefaultLocale")
    private void log_thread(String format, Object... args) {
        String s = String.format(format, args);
        s = String.format("[Thread %d] %s", Thread.currentThread().getId(), s);
        Log.d(TAG, s);
    }
}
