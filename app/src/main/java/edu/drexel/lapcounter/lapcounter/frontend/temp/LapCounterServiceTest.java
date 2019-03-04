package edu.drexel.lapcounter.lapcounter.frontend.temp;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

import edu.drexel.lapcounter.lapcounter.R;
import edu.drexel.lapcounter.lapcounter.backend.SimpleMessageReceiver;
import edu.drexel.lapcounter.lapcounter.backend.ble.BLEService;
import edu.drexel.lapcounter.lapcounter.backend.lapcounter.LapCounterService;
import edu.drexel.lapcounter.lapcounter.backend.lapcounter.LocationStateMachine;

import static edu.drexel.lapcounter.lapcounter.backend.ble.BLEComm.ACTION_CONNECTED;
import static edu.drexel.lapcounter.lapcounter.backend.ble.BLEComm.ACTION_DISCONNECTED;
import static edu.drexel.lapcounter.lapcounter.backend.ble.BLEComm.ACTION_RECONNECTED;
import static edu.drexel.lapcounter.lapcounter.backend.ble.RSSIManager.ACTION_RSSI_AND_DIR_AVAILABLE;
import static edu.drexel.lapcounter.lapcounter.backend.ble.RSSIManager.DEFAULT_DELTAS_WINDOW_SIZE;
import static edu.drexel.lapcounter.lapcounter.backend.ble.RSSIManager.DEFAULT_MOVING_AVERAGE_SIZE;
import static edu.drexel.lapcounter.lapcounter.backend.ble.RSSIManager.EXTRA_DIRECTION;
import static edu.drexel.lapcounter.lapcounter.backend.ble.RSSIManager.EXTRA_RSSI;
import static edu.drexel.lapcounter.lapcounter.backend.lapcounter.DisconnectManager.ACTION_MISSED_LAPS;
import static edu.drexel.lapcounter.lapcounter.backend.lapcounter.LapCounter.ACTION_LAP_COUNT_UPDATED;
import static edu.drexel.lapcounter.lapcounter.backend.lapcounter.LocationStateMachine.ACTION_STATE_TRANSITION;
import static edu.drexel.lapcounter.lapcounter.backend.lapcounter.LocationStateMachine.EXTRA_STATE_AFTER;
import static edu.drexel.lapcounter.lapcounter.backend.lapcounter.LocationStateMachine.EXTRA_STATE_BEFORE;

public class LapCounterServiceTest extends AppCompatActivity {
    private static final String TAG = LapCounterServiceTest.class.getSimpleName();

    public final static String EXTRA_DEVICE_NAME =
            "edu.drexel.lapcounter.lapcounter.frontend.temp.EXTRA_DEVICE_NAME";

    public final static String EXTRA_DEVICE_ADDRESS =
            "edu.drexel.lapcounter.lapcounter.frontend.temp.EXTRA_DEVICE_ADDRESS";

    private TextView mLog = null;
    private Button mStartOrStop;
    private TextView mRssi;
    private TextView mDir;
    private TextView mState;

    private final static SimpleDateFormat TIME_FORMATTER = new SimpleDateFormat("HH:mm:ss");

    private final static String PUCK_ADDRESS = "D1:AA:19:79:8A:18";
    private BLEService mBleService;

    private static final int REQUEST_ENABLE_BT = 2;

    private ServiceConnection mBleServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBleService = ((BLEService.LocalBinder) service).getService();
            mBleService.setRssiManagerWindowSizes(DEFAULT_DELTAS_WINDOW_SIZE,
                                                  DEFAULT_MOVING_AVERAGE_SIZE);

            connect();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBleService = null;
        }
    };

    private void connect() {
        log("Connecting...");
        mBleService.connectToDevice(PUCK_ADDRESS);
    }

    private LapCounterService mLapCounterService;

    private ServiceConnection mLapCounterServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mLapCounterService = ((LapCounterService.LocalBinder) service).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mLapCounterService = null;
        }
    };
    private SimpleMessageReceiver.MessageHandler mOnStateTransition = new SimpleMessageReceiver.MessageHandler() {
        @Override
        public void onMessage(Intent message) {
            LocationStateMachine.State before = (LocationStateMachine.State) message.getSerializableExtra(EXTRA_STATE_BEFORE);
            LocationStateMachine.State after = (LocationStateMachine.State) message.getSerializableExtra(EXTRA_STATE_AFTER);

            mState.setText(String.format("before: %s, after: %s", before, after));
        }
    };

    private SimpleMessageReceiver.MessageHandler mOnConnect = new SimpleMessageReceiver.MessageHandler() {
        @Override
        public void onMessage(Intent message) {
            mStartOrStop.setEnabled(true);
        }
    };

    private SimpleMessageReceiver.MessageHandler mOnDisconnect = new SimpleMessageReceiver.MessageHandler() {
        @Override
        public void onMessage(Intent message) {
            mStartOrStop.setEnabled(false);
            connect();
        }
    };

    private String getLast(String s, String delimiter) {
        String[] pieces = s.split(Pattern.quote(delimiter));

        if (pieces.length == 0) {
            return s;
        }

        return pieces[pieces.length - 1];
    }

    private String getLast(String s) {
        return getLast(s, ".");
    }

    private void log(String extra, Object value) {
        log(String.format("%s, %s", getLast(extra), value), false);
    }

    private void logExtras(Intent message) {
        Bundle bundle = message.getExtras();

        if (bundle == null) {
            return;
        }

        for (String extra : bundle.keySet()) {
            log(extra, bundle.get(extra));
        }
    }

    private final SimpleMessageReceiver.MessageHandler mDump = new SimpleMessageReceiver.MessageHandler() {
        @Override
        public void onMessage(Intent message) {
            log(getLast(message.getAction()));
            logExtras(message);
            log("", false);
        }
    };

    private final SimpleMessageReceiver.MessageHandler mOnRssiAndDir = new SimpleMessageReceiver.MessageHandler() {
        @Override
        public void onMessage(Intent message) {
            double rssi = message.getDoubleExtra(EXTRA_RSSI, 0.0);
            int dir = message.getIntExtra(EXTRA_DIRECTION, 0);

            mRssi.setText(Double.toString(rssi));
            mDir.setText(Integer.toString(dir));
        }
    };

    private final SimpleMessageReceiver mReceiver = new SimpleMessageReceiver();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lap_counter_service_test);

        mLog = findViewById(R.id.log);
        log("onCreate");

        mStartOrStop = findViewById(R.id.start_or_stop_btn);
        mRssi = findViewById(R.id.filtered_rssi);
        mDir = findViewById(R.id.dir);
        mState = findViewById(R.id.state);

        requestBluetoothPermission();
    }

    private void requestBluetoothPermission() {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                bindServices();
                registerHandlers();
                mReceiver.attach(this);
            }
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
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

    public void onClickStartOrStop(View view) {
        if (mStartOrStop.getText().equals("Start")) {
            mBleService.startRssiRequests();
            mStartOrStop.setText("Stop");
        } else {
            mBleService.stopRssiRequests();
            mStartOrStop.setText("Start");
        }
    }

    private void register(String action) {
        mReceiver.registerHandler(action, mDump);
    }

    private void registerHandlers() {
        register(ACTION_CONNECTED);
        register(ACTION_RECONNECTED);
        register(ACTION_DISCONNECTED);
        register(ACTION_MISSED_LAPS);
        register(ACTION_LAP_COUNT_UPDATED);
        register(ACTION_STATE_TRANSITION);

        mReceiver.registerHandler(ACTION_RSSI_AND_DIR_AVAILABLE, mOnRssiAndDir);
        mReceiver.registerHandler(ACTION_STATE_TRANSITION, mOnStateTransition);
        mReceiver.registerHandler(ACTION_CONNECTED, mOnConnect);
        mReceiver.registerHandler(ACTION_DISCONNECTED, mOnDisconnect);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        log("onDestroy");
        mReceiver.detach(this);

        if (mBleService != null) {
            mBleService.disconnectDevice();
            unbindServices();
        }

        mBleService = null;
        mLapCounterService = null;
    }

    private void log(String message, boolean addTimestamp) {
        if (addTimestamp) {
            String now = TIME_FORMATTER.format(new Date());
            mLog.append(String.format("[%s] %s\n", now, message));
        } else {
            mLog.append(message + "\n");
        }
    }

    private void log(String message) {
        log(message, true);
    }

    @Override
    protected void onStart() {
        super.onStart();
        log("onStart");
    }

    @Override
    protected void onStop() {
        super.onStop();
        log("onStop");
    }

    @Override
    protected void onPause() {
        super.onPause();
        log("onPause");
    }

    @Override
    protected void onResume() {
        super.onResume();
        log("onResume");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        log("onRestart");
    }
}
