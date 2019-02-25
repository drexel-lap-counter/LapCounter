package edu.drexel.lapcounter.lapcounter.frontend;

import android.annotation.SuppressLint;
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
import edu.drexel.lapcounter.lapcounter.backend.ble.RssiCollector;
import edu.drexel.lapcounter.lapcounter.backend.SimpleMessageReceiver;
import edu.drexel.lapcounter.lapcounter.backend.ble.BLEComm;
import edu.drexel.lapcounter.lapcounter.backend.ble.BLEService;

public class CalibrateDeviceActivity extends AppCompatActivity {
    private final static String TAG = CalibrateDeviceActivity.class.getSimpleName();

    private static String qualify(String s) {
        return CalibrateDeviceActivity.class.getPackage().getName() + "." + s;
    }

    public static final String EXTRAS_DEVICE_NAME = qualify("DEVICE_NAME");
    public static final String EXTRAS_DEVICE_ADDRESS = qualify("DEVICE_ADDRESS");
    public static final String EXTRAS_CALIBRATED_THRESHOLD = qualify("CALIBRATED_THRESHOLD");

    private final static int PRINT_COLLECTOR_STATS_FREQ_MS = 500;

    private TextView mCalibrateInfo;
    private Button mCalibrate;
    private Button mDone;

    private BLEService mBleService;
    private String mDeviceAddress;

    private final RssiCollector mRssiCollector = new RssiCollector();
    private long mLastPrintMillis;

    private final SimpleMessageReceiver mReceiver = new SimpleMessageReceiver();

    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            BLEService.LocalBinder binder = (BLEService.LocalBinder)service;
            mBleService = binder.getService();
            mBleService.connect(mDeviceAddress);
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
        mDone = findViewById(R.id.done);

        Intent intent = getIntent();
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

        Intent gattServiceIntent = new Intent(this, BLEService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

        registerHandlers();
    }

    private void registerHandlers() {
        mReceiver.registerHandler(BLEComm.ACTION_CONNECTED, onConnect);
        mReceiver.registerHandler(BLEComm.ACTION_DISCONNECTED, onDisconnect);
        mReceiver.registerHandler(BLEComm.ACTION_RAW_RSSI_AVAILABLE, onRssi);
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
            mBleService.connect(mDeviceAddress);
            mCalibrate.setEnabled(false);
            mDone.setEnabled(false);
        }
    };

    private final SimpleMessageReceiver.MessageHandler onRssi = new SimpleMessageReceiver.MessageHandler() {
        @Override
        public void onMessage(Intent message) {
            int rssi = message.getIntExtra(BLEComm.EXTRA_RAW_RSSI, 0);

            if (rssi == 0 || !mRssiCollector.isEnabled()) {
                return;
            }

            mRssiCollector.collect(rssi);

            long currentMillis = System.currentTimeMillis();
            long timeSinceLastPrint = currentMillis - mLastPrintMillis;

            if (timeSinceLastPrint >= PRINT_COLLECTOR_STATS_FREQ_MS) {
                mCalibrateInfo.setText(mRssiCollector.toString());
                mLastPrintMillis = currentMillis;
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();

        mReceiver.attach(this);

        if (mBleService != null) {
            final boolean result = mBleService.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }
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
        unbindService(mServiceConnection);
        mBleService = null;
    }

    public void calibrate(View view) {
        if (mRssiCollector.isEnabled()) {
            mRssiCollector.disable();
            mDone.setEnabled(true);
            mCalibrate.setText(R.string.label_calibrate);
            mCalibrateInfo.setText(mRssiCollector.toString());
            return;
        }

        mDone.setEnabled(false);
        mRssiCollector.enable();
        mCalibrate.setText(getString(R.string.label_stop));
    }

    public void done(View view) {
        double threshold = mRssiCollector.median();
        Intent result = getIntent();
        result.putExtra(EXTRAS_CALIBRATED_THRESHOLD, threshold);
        setResult(RESULT_OK, result);
        finish();
    }

    @SuppressLint("DefaultLocale")
    private void log_thread(String format, Object... args) {
        String s = String.format(format, args);
        s = String.format("[Thread %d] %s", Thread.currentThread().getId(), s);
        Log.d(TAG, s);
    }
}
