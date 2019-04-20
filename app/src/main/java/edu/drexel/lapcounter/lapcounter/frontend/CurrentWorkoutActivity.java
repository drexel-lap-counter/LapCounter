package edu.drexel.lapcounter.lapcounter.frontend;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import edu.drexel.lapcounter.lapcounter.R;
import edu.drexel.lapcounter.lapcounter.frontend.navigationbar.NavBar;

public class CurrentWorkoutActivity extends AppCompatActivity {
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

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    private void requestBluetoothPermission() {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                // todo: ble init
            }
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    private void connect() {
        // Get the selected device from shared preferences (if there is one)
        SharedPreferences prefs = getSharedPreferences(
                DeviceSelectActivity.PREFS_KEY, Context.MODE_PRIVATE);
        String mac = prefs.getString(DeviceSelectActivity.KEY_DEVICE_ADDRESS, null);

        if (mac == null) {
            mDebugConnectLabel.setText(R.string.label_no_device_selected);
            // TODO: What should happen if the user does not have a currently selected device?
            // Should they be redirected to the DeviceSelectActivity? Should the
            // CurrentWorkoutActivity simply say "No device selected." and become inert?
            // We need to discuss transitions between this and other activities.
            return;
        }

        String connectMessage = getResources().getString(R.string.label_connecting, mac);
        mDebugConnectLabel.setText(connectMessage);

        // todo: ble connect
    }

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

}
