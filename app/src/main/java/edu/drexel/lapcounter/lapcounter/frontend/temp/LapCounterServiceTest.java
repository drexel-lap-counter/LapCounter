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
import edu.drexel.lapcounter.lapcounter.backend.SimpleMessageReceiver;
import edu.drexel.lapcounter.lapcounter.backend.ble.BLEService;
import edu.drexel.lapcounter.lapcounter.backend.lapcounter.LapCounterService;

public class LapCounterServiceTest extends AppCompatActivity {
    private static final String TAG = LapCounterServiceTest.class.getSimpleName();

    public final static String EXTRA_DEVICE_NAME =
            "edu.drexel.lapcounter.lapcounter.frontend.temp.EXTRA_DEVICE_NAME";

    public final static String EXTRA_DEVICE_ADDRESS =
            "edu.drexel.lapcounter.lapcounter.frontend.temp.EXTRA_DEVICE_ADDRESS";

    private TextView mLog = null;

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

    private final SimpleMessageReceiver.MessageHandler mDump = new SimpleMessageReceiver.MessageHandler() {
        @Override
        public void onMessage(Intent message) {
            Bundle bundle = message.getExtras();
            if (bundle == null)
                return;

            for (String key : bundle.keySet()) {
                Object value = bundle.get(key);
                String[] packages = key.split(".");
                String shortKey = packages[packages.length - 1];
                String msg = String.format("%s: %s", shortKey, value.toString());
                log(msg);
            }
        }
    };

    private final SimpleMessageReceiver mReceiver = new SimpleMessageReceiver();

    private String getStringExtra(String extra) {
        return getIntent().getStringExtra(extra);
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

        mLog = findViewById(R.id.log);
        bindServices();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindServices();
        mBleService = null;
        mLapCounterService = null;
    }

    private void log(String message) {
        String newLog = message + "\n" + mLog.getText();
        mLog.setText(newLog);
    }
}
