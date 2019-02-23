package edu.drexel.lapcounter.lapcounter.frontend.temp;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

import edu.drexel.lapcounter.lapcounter.R;
import edu.drexel.lapcounter.lapcounter.backend.ble.BLEService;
import edu.drexel.lapcounter.lapcounter.backend.lapcounter.LapCounterService;

public class LapCounterServiceTest extends AppCompatActivity {
    private static final String TAG = LapCounterServiceTest.class.getSimpleName();

    public final static String EXTRA_DEVICE_NAME =
            "edu.drexel.lapcounter.lapcounter.frontend.temp.EXTRA_DEVICE_NAME";

    public final static String EXTRA_DEVICE_ADDRESS =
            "edu.drexel.lapcounter.lapcounter.frontend.temp.EXTRA_DEVICE_ADDRESS";

    private final Map<Integer, TextView> mTextViews = new HashMap<>();

    private final Map<String, Service> mServices = new HashMap<>();

    private BLEService mBleService;

    private ServiceConnection mBleServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
//            mBleService = ((BLEService.LocalBinder) service).getService();
//
//            if (!mBleService.initialize()) {
//                Log.e(TAG, "Unable to initialize Bluetooth");
//                return;
//            }
//
//            // Automatically connects to the device upon successful start-up initialization.
//            mBleService.connect(getStringExtra(EXTRA_DEVICE_ADDRESS));
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBleService = null;
        }
    };

    private LapCounterService mLapCounterService;

    private ServiceConnection mLapCounterServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mLapCounterService = null;
        }
    };

    private void storeTextView(int id) {
        mTextViews.put(id, (TextView) findViewById(id));
    }

    private void storeTextViews() {
        storeTextView(R.id.DeviceName);
        storeTextView(R.id.DeviceAddress);
    }

    private String getStringExtra(String extra) {
        return getIntent().getStringExtra(extra);
    }

    private void displayDeviceNameAddress() {
        mTextViews.get(R.id.DeviceName).setText(getStringExtra(EXTRA_DEVICE_NAME));
        mTextViews.get(R.id.DeviceAddress).setText(getStringExtra(EXTRA_DEVICE_ADDRESS));
    }

    private void bindService(Class serviceClass, ServiceConnection connection) {
        Intent serviceIntent = new Intent(this, serviceClass);
        bindService(serviceIntent, connection, BIND_AUTO_CREATE);
    }

    private void bindServices() {
        bindService(BLEService.class, mBleServiceConnection);
        bindService(LapCounterService.class, mLapCounterServiceConnection);
    }

    private void unbindServices() {
        unbindService(mBleServiceConnection);
        unbindService(mLapCounterServiceConnection);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lap_counter_service_test);

        storeTextViews();
        displayDeviceNameAddress();
    }

    @Override
    protected void onStart() {
        super.onStart();
        bindServices();
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindServices();
        mBleService = null;
    }
}
