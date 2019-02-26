package edu.drexel.lapcounter.lapcounter.frontend.temp;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

import edu.drexel.lapcounter.lapcounter.R;
import edu.drexel.lapcounter.lapcounter.backend.SimpleMessageReceiver;
import edu.drexel.lapcounter.lapcounter.backend.ble.BLEService;
import edu.drexel.lapcounter.lapcounter.backend.lapcounter.LapCounterService;

import static edu.drexel.lapcounter.lapcounter.backend.ble.BLEComm.ACTION_CONNECTED;
import static edu.drexel.lapcounter.lapcounter.backend.ble.BLEComm.ACTION_DISCONNECTED;
import static edu.drexel.lapcounter.lapcounter.backend.ble.BLEComm.ACTION_RAW_RSSI_AVAILABLE;
import static edu.drexel.lapcounter.lapcounter.backend.ble.RSSIManager.ACTION_RSSI_AND_DIR_AVAILABLE;
import static edu.drexel.lapcounter.lapcounter.backend.lapcounter.DisconnectManager.ACTION_MISSED_LAPS;
import static edu.drexel.lapcounter.lapcounter.backend.lapcounter.LapCounter.ACTION_LAP_COUNT_UPDATED;
import static edu.drexel.lapcounter.lapcounter.backend.lapcounter.LocationStateMachine.ACTION_STATE_TRANSITION;

public class LapCounterServiceTest extends AppCompatActivity {
    private static final String TAG = LapCounterServiceTest.class.getSimpleName();

    public final static String EXTRA_DEVICE_NAME =
            "edu.drexel.lapcounter.lapcounter.frontend.temp.EXTRA_DEVICE_NAME";

    public final static String EXTRA_DEVICE_ADDRESS =
            "edu.drexel.lapcounter.lapcounter.frontend.temp.EXTRA_DEVICE_ADDRESS";

    private TextView mLog = null;
    private Button mStartOrStop;

    private final static SimpleDateFormat TIME_FORMATTER = new SimpleDateFormat("HH:mm:ss");

    private final static String PUCK_ADDRESS = "D1:AA:19:79:8A:18";
    private BLEService mBleService;

    private ServiceConnection mBleServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBleService = ((BLEService.LocalBinder) service).getService();
            mBleService.connect(PUCK_ADDRESS);
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

    private void log(String action, String extra, Object value) {
        log(String.format("%s, %s, %s", getLast(action), getLast(extra), value));
    }

    private void log(String extra, Object value) {
        log(String.format("%s, %s", getLast(extra), value));
    }

    private final SimpleMessageReceiver.MessageHandler mDump = new SimpleMessageReceiver.MessageHandler() {
        @Override
        public void onMessage(Intent message) {
            Bundle bundle = message.getExtras();

            if (bundle == null)
                return;

//            String action = getLast(message.getAction());

            for (String extra : bundle.keySet()) {
                log(extra, bundle.get(extra));
            }
        }
    };

    private final SimpleMessageReceiver mReceiver = new SimpleMessageReceiver();

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
        mStartOrStop = findViewById(R.id.start_or_stop_btn);

        log("onCreate");
        bindServices();
        registerHandlers();
        mReceiver.attach(this);
    }

    public void onClickStartOrStop(View view) {
        if (mStartOrStop.getText().equals("Start")) {
//            start();
            mStartOrStop.setText("Stop");
        } else {
//            stop();
            mStartOrStop.setText("Start");
        }
    }

    private void register(String action) {
        mReceiver.registerHandler(action, mDump);
    }

    private void registerHandlers() {
        register(ACTION_CONNECTED);
        register(ACTION_DISCONNECTED);
//        register(ACTION_RAW_RSSI_AVAILABLE);
        register(ACTION_RSSI_AND_DIR_AVAILABLE);
        register(ACTION_MISSED_LAPS);
        register(ACTION_LAP_COUNT_UPDATED);
        register(ACTION_STATE_TRANSITION);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        log("onDestroy");
        mReceiver.detach(this);
        unbindServices();
        mBleService = null;
        mLapCounterService = null;
    }

    private void log(String message) {
        String now = TIME_FORMATTER.format(new Date());
        mLog.append(String.format("[%s] %s\n", now, message));
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
