package edu.drexel.lapcounter.lapcounter.frontend;

import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
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
import android.widget.Toast;
import android.support.v7.app.AlertDialog;

import java.time.Duration;
import java.util.Date;

import edu.drexel.lapcounter.lapcounter.R;
import edu.drexel.lapcounter.lapcounter.backend.Database.Workout.Workout;
import edu.drexel.lapcounter.lapcounter.backend.Database.Workout.WorkoutViewModel;
import edu.drexel.lapcounter.lapcounter.backend.SimpleMessageReceiver;
import edu.drexel.lapcounter.lapcounter.backend.ble.BLEComm;
import edu.drexel.lapcounter.lapcounter.backend.ble.BLEService;
import edu.drexel.lapcounter.lapcounter.backend.lapcounter.LapCounter;
import edu.drexel.lapcounter.lapcounter.backend.lapcounter.LapCounterService;
import edu.drexel.lapcounter.lapcounter.frontend.navigationbar.NavBar;

import static edu.drexel.lapcounter.lapcounter.backend.ble.RSSIManager.DEFAULT_DELTAS_WINDOW_SIZE;
import static edu.drexel.lapcounter.lapcounter.backend.ble.RSSIManager.DEFAULT_MOVING_AVERAGE_SIZE;

//import android.support.v4.content.LocalBroadcastManager;

/**
 * The main screen for recording workouts.
 */
public class CurrentWorkoutActivity extends AppCompatActivity {
    private static final String TAG = CurrentWorkoutActivity.class.getSimpleName();

    private final NavBar mNavBar = new NavBar(this, R.id.navigation_home);

    private Button startResumeButton;
    private Button pauseButton;
    private Button restartButton;
    private Button saveButton;

    // Unique ID for the Bluetooth request Intent
    private static final int REQUEST_ENABLE_BT = 2;

    //    private int mFakeLapCount = 0;
    private TextView timerValue;
    private String mTimerFormat;
    private Date startTime;
    private Date pauseTime;
    private Handler customHandler = new Handler();
    private boolean isPaused = false;

    long timeInMilliseconds = 0L;
    long timeSwapBuff = 0L;
    long updatedTime = 0L;

    // Text fields in the activity
    private TextView mCounter;

    private TextView mDebugConnectLabel;
    private int mLapCount;

    private String mDeviceAddress;

    private void pause() {
        // If we're already paused, do nothing.
        if (isPaused)
            return;

        // Save the paused time to use as
        pauseTime = new Date();

        timeSwapBuff += timeInMilliseconds;
        customHandler.removeCallbacks(updateTimerThread);

        startResumeButton.setEnabled(true);
        pauseButton.setEnabled(false);

        isPaused = true;
        mBleService.stopRssiRequests();
    }

    private void saveWorkout() {
        Workout workout = new Workout();
        workout.setDeviceMAC(loadDeviceAddress());
        workout.setStartDate(startTime);
        workout.setEndDate(pauseTime);
        workout.setLaps(mLapCount);
        int poolLength = loadPoolLength();

        workout.setPoolLength(poolLength);
        workout.setPoolUnits(loadPoolUnits());
        workout.setTotalDistanceTraveled(mLapCount * poolLength);

        WorkoutViewModel wvm = ViewModelProviders.of(this).get(WorkoutViewModel.class);
        wvm.insert(workout);

        Toast.makeText(this, R.string.save_workout_successful, Toast.LENGTH_SHORT).show();

        startResumeButton.setText(R.string.startButtonLabel);
        startResumeButton.setEnabled(true);
        pauseButton.setEnabled(false);
        saveButton.setEnabled(false);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_workout);

        // In the final version, use R.string.<string id> for titles
        getSupportActionBar().setTitle(R.string.title_current_workout);

        mNavBar.init();

        clearCalibratingFlag();

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
                startTime = new Date();
                customHandler.postDelayed(updateTimerThread, 0);

                startResumeButton.setEnabled(false);
                startResumeButton.setText(R.string.resumeButtonLabel);
                pauseButton.setEnabled(true);
                saveButton.setEnabled(true);

                isPaused = false;
                mBleService.startRssiRequests();
            }
        });


        pauseButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                pause();
            }
        });

        restartButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                pause();
                onButtonShowPopupWindowClick(view);
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                pause();
                saveWorkout();
            }
        });

        mCounter = findViewById(R.id.lapCount);
        mCounter.setText("0");
        // Register callbacks with the receiver
        mReceiver.registerHandler(LapCounter.ACTION_LAP_COUNT_UPDATED, updateUI);
        mReceiver.registerHandler(BLEComm.ACTION_CONNECTED, onConnect);
        mReceiver.registerHandler(BLEComm.ACTION_RECONNECTED, onReconnect);
        mReceiver.registerHandler(BLEComm.ACTION_DISCONNECTED, onDisconnect);

        requestBluetoothPermission();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mReceiver.detach(this);

        if (mBleService != null) {
            mBleService.disconnectDevice();
            unbindServices();
        }

        mBleService = null;
        mLapCounterService = null;
    }

    private void alertToSaveBeforeRestart(String alertText) {
        pause();

        if (updatedTime <= 0 ) {
            // User hasn't started a workout.
            restartWorkout();
            return;
        }

        // Ask if they'd like to save their current workout.
        new AlertDialog.Builder(this)
                .setMessage(alertText)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        saveWorkout();
                        restartWorkout();
                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        restartWorkout();
                    }
                })
                .show();
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Get the current bluetooth device address from
        String currentDevice = loadDeviceAddress();

        // There are many situtations where onStart() gets called. Let's make some flags to
        // distinguish them
        boolean oldDeviceExists = mDeviceAddress != null;
        boolean newDeviceExists = currentDevice != null;

        // This checks that both old and new device addresses exist and are equal in MAC address.
        boolean devicesSame = oldDeviceExists
                && newDeviceExists
                && mDeviceAddress.equals(currentDevice);

        boolean servicesReady = mLapCounterService != null && mBleService != null;

        // Check if the addresses are different. Note that we need to make sure at least one
        // device is non-null for them to be different.
        boolean bothNull = !oldDeviceExists && !newDeviceExists;
        boolean addressChanged = !devicesSame && !bothNull;

        // Check for a flag in shared preferences that indicates that we just calibrated a device
        boolean justCalibrated = wasCalibrating();

        // If the address changed or we calibrated, the lap counting will have to restart
        boolean deviceSettingsChanged = addressChanged || justCalibrated;


        // Step 1. Disconnect the old device if it exists and the old device did not change.
        if (oldDeviceExists && !devicesSame)
            mBleService.disconnectDevice();

        // Step 2. If device settings changed at all (including deleting the device) prompt
        // the user to save the progress before restarting;
        if (servicesReady && deviceSettingsChanged) {
            String msg = "Since you changed device settings mid-workout, the workout must be "
                + "stopped. Would you like to save your progress?";
            alertToSaveBeforeRestart(msg);
        }

        // Step 3. Set the new device address
        mDeviceAddress = currentDevice;

        // Step 4. Connect to the new device if there is one
        if (servicesReady && newDeviceExists && !devicesSame)
            connect();
    }

    private void requestBluetoothPermission() {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                bindService(BLEService.class, mBleServiceConnection);
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

    private void unbindServices() {
        unbindService(mBleServiceConnection);
        unbindService(mLapCounterServiceConnection);
    }

    private BLEService mBleService;

    private ServiceConnection mBleServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBleService = ((BLEService.LocalBinder) service).getService();
            mBleService.setRssiManagerWindowSizes(DEFAULT_DELTAS_WINDOW_SIZE,
                    DEFAULT_MOVING_AVERAGE_SIZE);

            bindService(LapCounterService.class, mLapCounterServiceConnection);
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
            mLapCounterService = ((LapCounterService.LocalBinder) service).getService();
            connect();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mLapCounterService = null;
        }
    };

    private String loadDeviceAddress() {
        // Get the selected device from shared preferences (if there is one)
        SharedPreferences prefs = getSharedPreferences(
                DeviceSelectActivity.PREFS_KEY, Context.MODE_PRIVATE);
        return prefs.getString(DeviceSelectActivity.KEY_DEVICE_ADDRESS, null);
    }

    private int loadPoolLength() {
        SharedPreferences prefs = getSharedPreferences(
                PoolSizeActivity.poolSizePreferences, Context.MODE_PRIVATE);
        return prefs.getInt(PoolSizeActivity.poolSizeKey, PoolSizeActivity.defPoolSize);
    }

    private String loadPoolUnits() {
        SharedPreferences prefs = getSharedPreferences(
                PoolSizeActivity.poolSizePreferences, Context.MODE_PRIVATE);
        return prefs.getString(PoolSizeActivity.poolUnitsKey, PoolSizeActivity.defPoolUnits);
    }

    private SharedPreferences getCalibrationSharedPrefs() {
        return getSharedPreferences(CalibrateDeviceActivity.PREFS_KEY, Context.MODE_PRIVATE);
    }

    private boolean wasCalibrating() {
        SharedPreferences prefs = getCalibrationSharedPrefs();
        return prefs.getBoolean(CalibrateDeviceActivity.KEY_WAS_CALIBRATING, false);
    }

    private void clearCalibratingFlag() {
        SharedPreferences.Editor editor = getCalibrationSharedPrefs().edit();
        editor.clear();
        editor.commit();
    }

    private void connect() {
        clearCalibratingFlag();

        if (mDeviceAddress == null) {
            mDebugConnectLabel.setText(R.string.label_no_device_selected);
            // TODO: What should happen if the user does not have a currently selected device?
            // Should they be redirected to the DeviceSelectActivity? Should the
            // CurrentWorkoutActivity simply say "No device selected." and become inert?
            // We need to discuss transitions between this and other activities.
            return;
        }

        String connectMessage = getResources().getString(R.string.label_connecting, mDeviceAddress);
        mDebugConnectLabel.setText(connectMessage);
        mBleService.connectToDevice(mDeviceAddress);
        mLapCounterService.updateThreshold(mDeviceAddress);

    }

    private Runnable updateTimerThread = new Runnable() {

        public void run() {
            Date now = new Date();
            timeInMilliseconds = now.getTime() - startTime.getTime();

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

    private void restartWorkout() {
        startTime = new Date();
        updatedTime = 0L;
        timeSwapBuff = 0L;
        timeInMilliseconds = 0L;
        timerValue.setText(R.string.timerVal);

//                mFakeLapCount = 0;
        mCounter.setText("0");

        startResumeButton.setText(R.string.startButtonLabel);
        startResumeButton.setEnabled(true);
        pauseButton.setEnabled(false);
        saveButton.setEnabled(false);

        mBleService.reset();
        mLapCounterService.reset();
    }

    /**
     * creates a confirmation dialog.
     * @param view
     */
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
                restartWorkout();
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

    /**
     * This class reduces the boilerplate of subscribing to custom events.
     *
     * It is probably best if these objects are always owned by the Context
     * (in this case the Activity, but Services also are Contexts)
     */
    private SimpleMessageReceiver mReceiver = new SimpleMessageReceiver();

    /**
     * Callback for parsing a message. This is an example of what a message callback
     * should
     */
    private SimpleMessageReceiver.MessageHandler updateUI = new SimpleMessageReceiver.MessageHandler() {
        @Override
        public void onMessage(Intent message) {
            if (wasCalibrating()) {
                Log.i(TAG, "Ignored lap count because of calibration.");
                return;
            }

            // Extract info from the Intent
            mLapCount = message.getIntExtra(LapCounter.EXTRA_LAP_COUNT, 0);

            //Update the TextView
            mCounter.setText(Integer.toString(mLapCount));

            Log.i(TAG, "Lap count is now " + mLapCount);
        }
    };

    private void setConnectText(String deviceAddress) {
        String s = getResources().getString(R.string.label_connected, deviceAddress);
        mDebugConnectLabel.setText(s);
    }

    private SimpleMessageReceiver.MessageHandler onConnect = new SimpleMessageReceiver.MessageHandler() {
        @Override
        public void onMessage(Intent message) {
            setConnectText(message.getStringExtra(BLEComm.EXTRA_DEVICE_ADDRESS));
            startResumeButton.setEnabled(true);
            restartButton.setEnabled(true);
        }
    };

    private SimpleMessageReceiver.MessageHandler onReconnect = new SimpleMessageReceiver.MessageHandler() {
        @Override
        public void onMessage(Intent message) {
            setConnectText(message.getStringExtra(BLEComm.EXTRA_DEVICE_ADDRESS));
        }
    };

    private SimpleMessageReceiver.MessageHandler onDisconnect = new SimpleMessageReceiver.MessageHandler() {
        @Override
        public void onMessage(Intent message) {
            if (wasCalibrating()) {
                Log.i(TAG, "Ignored disconnect event because user is calibrating a device.");
                return;
            }
            mDebugConnectLabel.setText(R.string.label_device_disconnected_try_reconnect);
            connect();
        }
    };
}
