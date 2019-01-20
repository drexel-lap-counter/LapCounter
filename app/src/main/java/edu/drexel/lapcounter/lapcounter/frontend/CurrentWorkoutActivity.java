package edu.drexel.lapcounter.lapcounter.frontend;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import edu.drexel.lapcounter.lapcounter.R;

public class CurrentWorkoutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_workout);

        // In the final version, use R.string.<string id> for titles
        getSupportActionBar().setTitle("Current Workout");
    }

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
