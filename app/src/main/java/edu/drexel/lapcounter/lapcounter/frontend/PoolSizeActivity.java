package edu.drexel.lapcounter.lapcounter.frontend;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import edu.drexel.lapcounter.lapcounter.R;

public class PoolSizeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pool_size);

        // In the final version, use R.string.<string id> for titles
        getSupportActionBar().setTitle("Pool Size");
    }
}
