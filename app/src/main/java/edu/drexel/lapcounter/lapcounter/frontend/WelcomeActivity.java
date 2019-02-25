package edu.drexel.lapcounter.lapcounter.frontend;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import edu.drexel.lapcounter.lapcounter.R;
import edu.drexel.lapcounter.lapcounter.frontend.navigationbar.NavBar;

/**
 * activity for the initial welcome screen
 */
public class WelcomeActivity extends AppCompatActivity {
    private final NavBar mNavBar = new NavBar(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        // In the final version, use R.string.<string id> for titles
        getSupportActionBar().setTitle("Welcome");

        mNavBar.init();
    }

    public void nextClicked(View view) {
        Intent intent = new Intent(this, CurrentWorkoutActivity.class);
        startActivity(intent);
    }
}
