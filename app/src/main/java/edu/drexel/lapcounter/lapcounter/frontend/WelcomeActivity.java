package edu.drexel.lapcounter.lapcounter.frontend;

import android.content.Intent;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import edu.drexel.lapcounter.lapcounter.R;
import edu.drexel.lapcounter.lapcounter.frontend.navigationbar.OnItemSelectedListener;

public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        // In the final version, use R.string.<string id> for titles
        getSupportActionBar().setTitle("Welcome");

        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(new OnItemSelectedListener(this));
        navigation.getMenu().getItem(0).setChecked(true);
    }

    public void nextClicked(View view) {
        Intent intent = new Intent(this, CurrentWorkoutActivity.class);
        startActivity(intent);
    }
}
