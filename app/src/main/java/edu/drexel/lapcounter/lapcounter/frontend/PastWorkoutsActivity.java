package edu.drexel.lapcounter.lapcounter.frontend;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import edu.drexel.lapcounter.lapcounter.R;

public class PastWorkoutsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_past_workouts);

        // In the final version, use R.string.<string id> for titles
        getSupportActionBar().setTitle("Past Workouts");
    }

    public void selectWorkout(View view) {
        Intent intent = new Intent(this, WorkoutDetailsActivity.class);
        startActivity(intent);
    }
}
