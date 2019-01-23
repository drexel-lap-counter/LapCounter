package edu.drexel.lapcounter.lapcounter.frontend;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import edu.drexel.lapcounter.lapcounter.R;
import edu.drexel.lapcounter.lapcounter.frontend.navigationbar.NavBar;

public class SettingsActivity extends AppCompatActivity {
    private final NavBar mNavBar = new NavBar(this, R.id.navigation_settings);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // In the final version, use R.string.<string id> for titles
        getSupportActionBar().setTitle("Settings");

        mNavBar.init();
    }

    public void setPoolSize(View view) {
        Intent intent = new Intent(this, PoolSizeActivity.class);
        startActivity(intent);
    }

    public void selectDevice(View view) {
        Intent intent = new Intent(this, DeviceSelectActivity.class);
        startActivity(intent);
    }
}
