package edu.drexel.lapcounter.lapcounter.frontend;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.os.Handler;
import android.os.SystemClock;
import android.widget.Button;
import android.widget.TextView;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.view.Gravity;
import android.view.MotionEvent;


import edu.drexel.lapcounter.lapcounter.R;
import edu.drexel.lapcounter.lapcounter.backend.SimpleMessageReceiver;
import edu.drexel.lapcounter.lapcounter.backend.ble.BLEComm;
import edu.drexel.lapcounter.lapcounter.backend.ble.BLEService;
import edu.drexel.lapcounter.lapcounter.backend.lapcounter.LapCounter;
import edu.drexel.lapcounter.lapcounter.backend.lapcounter.LapCounterService;
import edu.drexel.lapcounter.lapcounter.frontend.navigationbar.NavBar;

import static edu.drexel.lapcounter.lapcounter.backend.ble.RSSIManager.DEFAULT_DELTAS_WINDOW_SIZE;
import static edu.drexel.lapcounter.lapcounter.backend.ble.RSSIManager.DEFAULT_MOVING_AVERAGE_SIZE;

public class CurrentWorkoutActivity extends AppCompatActivity {
    private final NavBar mNavBar = new NavBar(this, R.id.navigation_home);

    private Button startButton;
    private Button pauseButton;
    private Button restartButton;
    private Button yesButton;
    private Button noButton;
    private Button resumeButton;

    public static final String ACTION_LAP_COUNT =
            "edu.drexel.lapcounter.lapcounter.ACTION_LAP_COUNT";
    public static final String EXTRA_CURRENT_LAP_COUNT =
            "edu.drexel.lapcounter.lapcounter.ACTION_LAP_COUNT";

    // Unique ID for the Bluetooth request Intent
    private static final int REQUEST_ENABLE_BT = 2;

    private int mFakeLapCount = 0;
    private TextView timerValue;
    private long startTime = 0L;
    private Handler customHandler = new Handler();
    private boolean isPaused = false;

    long timeInMilliseconds = 0L;
    long timeSwapBuff = 0L;
    long updatedTime = 0L;

    // Text fields in the activity
    private TextView mCounter;

    // Object for scheduling fake laps counted.
    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_workout);

        // In the final version, use R.string.<string id> for titles
        getSupportActionBar().setTitle("Current Workout");

        mNavBar.init();

        timerValue = findViewById(R.id.timerValue);

        startButton = findViewById(R.id.startButton);
        startButton.setEnabled(false);

        resumeButton = findViewById(R.id.resumeButton);
        resumeButton.setEnabled(false);

        pauseButton = findViewById(R.id.pauseButton);
        pauseButton.setEnabled(false);

        restartButton = findViewById(R.id.restartButton);
        restartButton.setEnabled(false);

        startButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                startTime = SystemClock.uptimeMillis();
                customHandler.postDelayed(updateTimerThread, 0);

                startButton.setVisibility(View.INVISIBLE);
                resumeButton.setVisibility(View.VISIBLE);
                resumeButton.setEnabled(false);
                pauseButton.setEnabled(true);

                mBleService.startRssiRequests();
            }
        });


        resumeButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                startTime = SystemClock.uptimeMillis();
                customHandler.postDelayed(updateTimerThread, 0);
                resumeButton.setEnabled(false);
                pauseButton.setEnabled(true);

                isPaused = false;
                mBleService.startRssiRequests();
            }
        });

        pauseButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {

                timeSwapBuff += timeInMilliseconds;
                customHandler.removeCallbacks(updateTimerThread);
                resumeButton.setEnabled(true);
                pauseButton.setEnabled(false);

                isPaused = true;
                mBleService.stopRssiRequests();
            }
        });



        restartButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                timeSwapBuff += timeInMilliseconds;
                customHandler.removeCallbacks(updateTimerThread);
                onButtonShowPopupWindowClick(view);
            }
        });

        mCounter = findViewById(R.id.lapCount);
        mCounter.setText("0");
        // Register callbacks with the receiver
        mReceiver.registerHandler(LapCounter.ACTION_LAP_COUNT_UPDATED, updateUI);
        mReceiver.registerHandler(BLEComm.ACTION_CONNECTED, onConnect);
        // TODO: Failed to connect event and somehow alert user?

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

    private void connect() {
        // TODO: Pull this from somewhere in the scary land of Android Room?
        final String PUCK_ADDRESS = "D1:AA:19:79:8A:18";
        mBleService.connectToDevice(PUCK_ADDRESS);
    }

    private Runnable updateTimerThread = new Runnable() {

        public void run() {

            timeInMilliseconds = SystemClock.uptimeMillis() - startTime;

            updatedTime = timeSwapBuff + timeInMilliseconds;
            int secs = (int) (updatedTime / 1000);
            int mins = secs / 60;
            secs = secs % 60;
            int milliseconds = (int) (updatedTime % 1000);
            timerValue.setText("" + mins + ":"
                    + String.format("%02d", secs) + ":"
                    + String.format("%03d", milliseconds));
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

        yesButton = (Button) popupView.findViewById(R.id.yesButton);
        yesButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                startTime = SystemClock.uptimeMillis();
                updatedTime = 0L;
                timeSwapBuff = 0L;
                timeInMilliseconds = 0L;
                mFakeLapCount = 0;  //we need to have a design talk about notifying the counter
                mCounter.setText("0");
                customHandler.postDelayed(updateTimerThread, 0);
                customHandler.removeCallbacks(updateTimerThread);
                resumeButton.setEnabled(true);
                pauseButton.setEnabled(false);
                popupWindow.dismiss();
            }
        } );

        noButton = popupView.findViewById(R.id.noButton);
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
            // Extract info from the Intent
            // TODO: Get the actual constant from LapCounter(Service?)
            int lapCount = message.getIntExtra(LapCounter.EXTRA_LAP_COUNT, 0);

            //Update the TextView
            mCounter.setText(Integer.toString(lapCount));
        }
    };

    private SimpleMessageReceiver.MessageHandler onConnect = new SimpleMessageReceiver.MessageHandler() {
        @Override
        public void onMessage(Intent message) {
            startButton.setEnabled(true);
            restartButton.setEnabled(true);
        }
    };

    /**
     * Simulate counting laps. This just increments the counter once a second
     */
    private void simulateLaps() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(!isPaused) {
                    mFakeLapCount += 2;

                    publishLapCount();
                }
                simulateLaps();
            }
        }, 1000);
    }

    private void publishLapCount() {
        // Create the intent, specifying the name of the event (i.e. "action")
        Intent intent = new Intent(ACTION_LAP_COUNT);

        // If needed, store some key-value pairs in the intent
        intent.putExtra(EXTRA_CURRENT_LAP_COUNT, mFakeLapCount);

        // Finally, publish the event. As long as you have a Context (Activity/Service/etc) you
        // can broadcast intents
        //sendBroadcast(intent);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}
