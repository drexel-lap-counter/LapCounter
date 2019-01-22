package edu.drexel.lapcounter.lapcounter.frontend;

import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import edu.drexel.lapcounter.lapcounter.R;
import edu.drexel.lapcounter.lapcounter.frontend.navigationbar.OnItemSelectedListener;

public class WorkoutDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_details);

        // In the final version, use R.string.<string id> for titles
        getSupportActionBar().setTitle("Workout Details");

        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(new OnItemSelectedListener(this));
        navigation.getMenu().getItem(1).setChecked(true);
    }
}
