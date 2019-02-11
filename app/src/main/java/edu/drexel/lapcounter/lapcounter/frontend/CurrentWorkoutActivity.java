package edu.drexel.lapcounter.lapcounter.frontend;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.os.Handler;
import android.os.SystemClock;
import android.widget.Button;
import android.widget.TextView;

import edu.drexel.lapcounter.lapcounter.R;
import edu.drexel.lapcounter.lapcounter.frontend.navigationbar.NavBar;

public class CurrentWorkoutActivity extends AppCompatActivity {
    private final NavBar mNavBar = new NavBar(this, R.id.navigation_home);

    private Button startButton;
    private Button pauseButton;
    private Button restartButton;

    private TextView timerValue;

    private long startTime = 0L;

    private Handler customHandler = new Handler();

    long timeInMilliseconds = 0L;
    long timeSwapBuff = 0L;
    long updatedTime = 0L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_workout);

        // In the final version, use R.string.<string id> for titles
        getSupportActionBar().setTitle("Current Workout");

        mNavBar.init();

        timerValue = (TextView) findViewById(R.id.timerValue);

        startButton = (Button) findViewById(R.id.startButton);

        startButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                startTime = SystemClock.uptimeMillis();
                customHandler.postDelayed(updateTimerThread, 0);

            }
        });

        pauseButton = (Button) findViewById(R.id.pauseButton);

        pauseButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {

                timeSwapBuff += timeInMilliseconds;
                customHandler.removeCallbacks(updateTimerThread);

            }
        });

        restartButton = (Button) findViewById(R.id.restartButton);

        restartButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {

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
}
