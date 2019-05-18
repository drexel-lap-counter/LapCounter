package edu.drexel.lapcounter.lapcounter.frontend;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import edu.drexel.lapcounter.lapcounter.backend.Database.Transition.TransitionRepository;
import edu.drexel.lapcounter.lapcounter.backend.Database.Workout.WorkoutRepository;

/**
 * The Splash screen displays our logo while the database is initializing.
 */
public class SplashActivity extends AppCompatActivity {

    /** Logging tag */
    private final static String TAG = SplashActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initDatabase();
        goToCurrentWorkoutScreen();
    }

    /**
     * When finished initializing the database, call this method to transition to the
     * Current Workout Screen
     */
    private void goToCurrentWorkoutScreen() {
        Intent intent = new Intent(this, CurrentWorkoutActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * Initialize the enum tables in the database
     */
    private void initDatabase() {
        // Initialize the enum tables
        try {
            // Initialize the Units table
            WorkoutRepository workoutRepo = new WorkoutRepository(getApplication());
            workoutRepo.initUnitsTable();


            // Initialze the transition table
            TransitionRepository transRepo = new TransitionRepository(getApplication());
            transRepo.initStatesTable();
        } catch (Exception e) {
            Log.e(TAG, "Error Connecting and Initalizing Database", e);
        }
    }
}

