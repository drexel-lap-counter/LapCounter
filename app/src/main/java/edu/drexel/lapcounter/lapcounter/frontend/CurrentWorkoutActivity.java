package edu.drexel.lapcounter.lapcounter.frontend;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import java.util.Locale;
import java.util.function.BiConsumer;

import androidx.annotation.NonNull;
import edu.drexel.lapcounter.lapcounter.R;
import edu.drexel.lapcounter.lapcounter.backend.ble.MovingAverage;
import edu.drexel.lapcounter.lapcounter.backend.ble.SlidingWindow;
import edu.drexel.lapcounter.lapcounter.frontend.navigationbar.NavBar;

import static edu.drexel.lapcounter.lapcounter.backend.ble.RSSIManager.DIRECTION_IN;
import static edu.drexel.lapcounter.lapcounter.backend.ble.RSSIManager.DIRECTION_OUT;

public class CurrentWorkoutActivity extends AppCompatActivity implements LapCounterBleManagerCallbacks {
    private final static String TAG = CurrentWorkoutActivity.class.getSimpleName();

    private final NavBar mNavBar = new NavBar(this, R.id.navigation_home);

    private Button startResumeButton;
    private Button pauseButton;
    private Button restartButton;
    private Button saveButton;

    // Unique ID for the Bluetooth request Intent
    private static final int REQUEST_ENABLE_BT = 2;

    private TextView timerValue;
    private String mTimerFormat;
    private long startTime = 0L;
    private Handler customHandler = new Handler();
    private boolean isPaused = false;

    long timeInMilliseconds = 0L;
    long timeSwapBuff = 0L;
    long updatedTime = 0L;

    // Text fields in the activity
    private TextView mCounter;

    private TextView mDebugConnectLabel;

    private LapCounterBleManager mBleManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_workout);

        // In the final version, use R.string.<string id> for titles
        getSupportActionBar().setTitle(R.string.title_current_workout);

        mNavBar.init();

        timerValue = findViewById(R.id.timerValue);
        mTimerFormat = getString(R.string.current_workout_timer_format);

        startResumeButton = findViewById(R.id.startResumeButton);
        startResumeButton.setEnabled(false);

        pauseButton = findViewById(R.id.pauseButton);
        pauseButton.setEnabled(false);

        restartButton = findViewById(R.id.restartButton);
        restartButton.setEnabled(false);

        saveButton = findViewById(R.id.saveButton);
        saveButton.setEnabled(false);

        mDebugConnectLabel = findViewById(R.id.debugConnectLabel);

        startResumeButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                startTime = SystemClock.uptimeMillis();
                customHandler.postDelayed(updateTimerThread, 0);

                startResumeButton.setEnabled(false);
                startResumeButton.setText(R.string.resumeButtonLabel);
                pauseButton.setEnabled(true);

                isPaused = false;

                readRssi();
            }
        });


        pauseButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                timeSwapBuff += timeInMilliseconds;
                customHandler.removeCallbacks(updateTimerThread);

                startResumeButton.setEnabled(true);
                pauseButton.setEnabled(false);

                isPaused = true;
            }
        });



        restartButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (!isPaused) {
                    pauseButton.performClick();
                }
                onButtonShowPopupWindowClick(view);
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (!isPaused) {
                    pauseButton.performClick();
                }
                // TODO: make version of this function for saving
                //onButtonShowPopupWindowClick(view);
            }
        });

        mCounter = findViewById(R.id.lapCount);
        mCounter.setText("0");

        requestBluetoothPermission();
    }

    private void readRssi() {
        mBleManager.readRssi().with(this::onRssi).done(unused_arg -> {
            // Poll infrequently to jump past noise.
            mBleManager.sleep(2500).enqueue(); // Hyperparameter 1

            if (!isPaused) {
                readRssi();
            }
        }).enqueue();
    }


    private Integer mPreviousRssi = null;
    private int mPreviousDirection = DIRECTION_OUT;
    private int mLapCount = 0;

    private void onRssi(BluetoothDevice device, int rssi) {
        rssi = Math.abs(rssi);

        Log.i(TAG, Integer.toString(rssi));

        if (mPreviousRssi == null) {
            mPreviousRssi = rssi;
            return;
        }

        int delta = rssi - mPreviousRssi;

        if (Math.abs(delta) < 10) { // Hyperparameter 2
            Log.i(TAG, "Ignored small delta of " + delta);
            return;
        }

        mPreviousRssi = rssi;

        int direction = (int)Math.signum(delta);

        // The swimmer flipped near the beginning of the pool.
        if (mPreviousDirection == DIRECTION_IN && direction == DIRECTION_OUT) {
            // So they completed 2 laps.
            ++mLapCount;
            mCounter.setText(String.format(Locale.US, "%d", mLapCount));
            Log.i(TAG, "Lap counted.");
        }

        mPreviousDirection = direction;
    }

    @Override
    protected void onDestroy() {
        if (mBleManager != null) {
            mBleManager.disconnect();
        }
        super.onDestroy();

    }

    private void requestBluetoothPermission() {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    }

    private static BluetoothAdapter getAdapter(Context c) {
        BluetoothManager m = (BluetoothManager) c.getSystemService(Context.BLUETOOTH_SERVICE);
        return m.getAdapter();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                mBleManager = new LapCounterBleManager(this);
                mBleManager.setGattCallbacks(this);
                connect();
            }
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


//    private void connect() {
//        // Get the selected device from shared preferences (if there is one)
//        SharedPreferences prefs = getSharedPreferences(
//                DeviceSelectActivity.PREFS_KEY, Context.MODE_PRIVATE);
//        String mac = prefs.getString(DeviceSelectActivity.KEY_DEVICE_ADDRESS, null);
//
//        if (mac == null) {
//            mDebugConnectLabel.setText(R.string.label_no_device_selected);
//            // TODO: What should happen if the user does not have a currently selected device?
//            // Should they be redirected to the DeviceSelectActivity? Should the
//            // CurrentWorkoutActivity simply say "No device selected." and become inert?
//            // We need to discuss transitions between this and other activities.
//            return;
//        }
//
//        String connectMessage = getResources().getString(R.string.label_connecting, mac);
//        mDebugConnectLabel.setText(connectMessage);
//
//        // todo: ble connect
//    }

    private Runnable updateTimerThread = new Runnable() {

        public void run() {
            timeInMilliseconds = SystemClock.uptimeMillis() - startTime;
            updatedTime = timeSwapBuff + timeInMilliseconds;
            int secs = (int) (updatedTime / 1000);
            int mins = secs / 60;
            secs = secs % 60;
            int milliseconds = (int) (updatedTime % 1000);

            String newTimerText = String.format(mTimerFormat, mins, secs, milliseconds);
            timerValue.setText(newTimerText);
            customHandler.postDelayed(this, 0);
        }

    };

    public void onButtonShowPopupWindowClick(View view) {

        // inflate the layout of the popup window
        LayoutInflater inflater = (LayoutInflater)
                getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.are_you_sure_popup, null);

        // create the popup window
        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = true; // lets taps outside the popup also dismiss it
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

        // show the popup window
        // which view you pass in doesn't matter, it is only used for the window tolken
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);

        // dismiss the popup window when touched
        popupView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                popupWindow.dismiss();
                return true;
            }
        });

        Button yesButton = popupView.findViewById(R.id.yesButton);
        yesButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                startTime = SystemClock.uptimeMillis();
                updatedTime = 0L;
                timeSwapBuff = 0L;
                timeInMilliseconds = 0L;
                timerValue.setText(R.string.timerVal);

//                mFakeLapCount = 0;
                mCounter.setText("0");

                startResumeButton.setText(R.string.startButtonLabel);
                startResumeButton.setEnabled(true);
                pauseButton.setEnabled(false);

                popupWindow.dismiss();
            }
        } );

        Button noButton = popupView.findViewById(R.id.noButton);
        noButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                popupWindow.dismiss();
            }
        } );
    }

    private void setConnectText(String deviceAddress) {
        String s = getResources().getString(R.string.label_connected, deviceAddress);
        mDebugConnectLabel.setText(s);
    }

    private void connect() {
        final String PUCK = "D1:AA:19:79:8A:18";
        mBleManager.connect(getAdapter(this).getRemoteDevice(PUCK))
                .useAutoConnect(true)
                .retry(300, 100)
                .enqueue();
    }

    @Override
    public void onDeviceConnecting(@NonNull BluetoothDevice device) {
        String connectMessage = getResources().getString(R.string.label_connecting,
                device.getAddress());
        mDebugConnectLabel.setText(connectMessage);
    }

    @Override
    public void onDeviceConnected(@NonNull BluetoothDevice device) {
        setConnectText(device.getAddress());
        startResumeButton.setEnabled(true);
        restartButton.setEnabled(true);
    }

    @Override
    public void onDeviceDisconnecting(@NonNull BluetoothDevice device) {
        mDebugConnectLabel.setText("Interesting. Disconnecting from " + device.getAddress());
    }

    @Override
    public void onDeviceDisconnected(@NonNull BluetoothDevice device) {
        mDebugConnectLabel.setText("Interesting. Disconnected from " + device.getAddress());
        connect();
    }

    @Override
    public void onLinkLossOccurred(@NonNull BluetoothDevice device) {
        mDebugConnectLabel.setText(R.string.label_device_disconnected_try_reconnect);
    }

    private void log(BluetoothDevice d, String s) {
        s = String.format(Locale.US, "Device %s (%s), %s", d.getAddress(), d.getName(), s);
        Log.i(TAG, s);
    }

    @Override
    public void onServicesDiscovered(@NonNull BluetoothDevice device, boolean optionalServicesFound) {
        log(device, "onServicesDiscovered()");
    }

    @Override
    public void onDeviceReady(@NonNull BluetoothDevice device) {
        log(device, "onDeviceReady()");
    }

    @Override
    public void onBondingRequired(@NonNull BluetoothDevice device) {
        log(device, "onBondingRequired()");

    }

    @Override
    public void onBonded(@NonNull BluetoothDevice device) {
        log(device, "onBonded()");
    }

    @Override
    public void onBondingFailed(@NonNull BluetoothDevice device) {
        log(device, "onBondingFailed()");
    }

    @Override
    public void onError(@NonNull BluetoothDevice device, @NonNull String message, int errorCode) {
        String s = String.format(Locale.ENGLISH, "Device %s, error %d: %s", device.getAddress(),
                errorCode, message);
        Log.e(TAG, s);
    }

    @Override
    public void onDeviceNotSupported(@NonNull BluetoothDevice device) {
        Log.e(TAG, String.format("Device %s not supported.", device.getAddress()));
    }
}
