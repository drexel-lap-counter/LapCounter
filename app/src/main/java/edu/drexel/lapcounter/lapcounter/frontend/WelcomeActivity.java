package edu.drexel.lapcounter.lapcounter.frontend;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import edu.drexel.lapcounter.lapcounter.R;

public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        // In the final version, use R.string.<string id> for titles
        getSupportActionBar().setTitle("Welcome");
    }

    public void nextClicked(View view) {
        Intent intent = new Intent(this, CurrentWorkoutActivity.class);
        startActivity(intent);
    }
}
