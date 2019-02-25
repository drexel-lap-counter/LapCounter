package edu.drexel.lapcounter.lapcounter.frontend;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import edu.drexel.lapcounter.lapcounter.R;

/**
 * activity for the splach/logo screen
 */
public class LoadingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading_screen);

        // In the final version, use R.string.<string id> for titles
        getSupportActionBar().setTitle("Splash Screen");
    }

    /**
     * Call this when the splash screen is done
     * to traansition to the Welcome screen.
     *
     * Right now, this is aattached to a button click, but
     * really it should be on a timer.
     */
    public void finishLoading(View view) {
        Intent intent = new Intent(this, WelcomeActivity.class);
        startActivity(intent);
    }
}
