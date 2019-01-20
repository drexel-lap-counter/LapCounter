package edu.drexel.lapcounter.lapcounter.frontend;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import edu.drexel.lapcounter.lapcounter.R;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // In the final version, use R.string.<string id> for titles
        getSupportActionBar().setTitle("Settings");
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
