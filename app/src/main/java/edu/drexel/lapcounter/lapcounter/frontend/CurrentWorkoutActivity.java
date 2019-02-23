package edu.drexel.lapcounter.lapcounter.frontend;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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
import edu.drexel.lapcounter.lapcounter.frontend.navigationbar.NavBar;

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

        timerValue = (TextView) findViewById(R.id.timerValue);

        startButton = (Button) findViewById(R.id.startButton);

        resumeButton = (Button) findViewById(R.id.resumeButton);

        mCounter = findViewById(R.id.lapCount);
        mCounter.setText("0");
        // Register callbacks with the receiver
        mReceiver.registerHandler(ACTION_LAP_COUNT, updateUI);



        startButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                startTime = SystemClock.uptimeMillis();
                customHandler.postDelayed(updateTimerThread, 0);

                startButton.setVisibility(View.INVISIBLE);
                resumeButton.setVisibility(View.VISIBLE);
                resumeButton.setEnabled(false);
                pauseButton.setEnabled(true);

                simulateLaps();
            }
        });

        resumeButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                startTime = SystemClock.uptimeMillis();
                customHandler.postDelayed(updateTimerThread, 0);
                resumeButton.setEnabled(false);
                pauseButton.setEnabled(true);
                onResume();
            }
        });

        pauseButton = (Button) findViewById(R.id.pauseButton);
        pauseButton.setEnabled(false);

        pauseButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {

                timeSwapBuff += timeInMilliseconds;
                customHandler.removeCallbacks(updateTimerThread);
                resumeButton.setEnabled(true);
                pauseButton.setEnabled(false);
                onPause();
            }
        });

        restartButton = (Button) findViewById(R.id.restartButton);

        restartButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                timeSwapBuff += timeInMilliseconds;
                customHandler.removeCallbacks(updateTimerThread);
                onButtonShowPopupWindowClick(view);
            }
        });
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

    // TODO: Maybe rename WorkoutHistory -> Analytics so it's less confusing?
    public void viewWorkoutHistory(View view) {
        Intent intent = new Intent(this, WorkoutHistoryActivity.class);
        startActivity(intent);
    }

    public void goHome(View view) {
        Intent intent = new Intent(this, WelcomeActivity.class);
        startActivity(intent);
    }

    public void viewPastWorkouts(View view) {
        Intent intent = new Intent(this, PastWorkoutsActivity.class);
        startActivity(intent);
    }

    public void configureSettings(View view) {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

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
                startTime = 0;
                customHandler.postDelayed(updateTimerThread, 0);
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
            int lapCount = message.getIntExtra(EXTRA_CURRENT_LAP_COUNT, 0);

            //Update the TextView
            mCounter.setText(Integer.toString(lapCount));
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        isPaused = false;
        // Enable the callbacks by registering the receiver
        // This is shorthand for registerReceiver()
        mReceiver.attach(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        isPaused = true;
        // Unsubscribe from events
        // This is shorthand for unregisterReceiver()
        mReceiver.detatch(this);
    }

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
        sendBroadcast(intent);
    }
}
