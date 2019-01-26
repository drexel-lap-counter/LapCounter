package edu.drexel.lapcounter.lapcounter.frontend;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import edu.drexel.lapcounter.lapcounter.R;
import edu.drexel.lapcounter.lapcounter.frontend.navigationbar.NavBar;

public class WorkoutDetailsActivity extends AppCompatActivity {
    private final NavBar mNavBar = new NavBar(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_details);

        // In the final version, use R.string.<string id> for titles
        getSupportActionBar().setTitle("Workout Details");

        mNavBar.init();
    }
}
