package edu.drexel.lapcounter.lapcounter.frontend.temp;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import edu.drexel.lapcounter.lapcounter.R;
import edu.drexel.lapcounter.lapcounter.backend.Helper;
import edu.drexel.lapcounter.lapcounter.backend.SimpleMessageReceiver;

/**
 * This Activity is a simple demo of how to send and receive messages. The new backend design
 * will use this extensively, but the UI stuff might find it useful too.
 *
 * TODO: Remove me once we're satisfied with how the new message passing works
 */
public class ReceiverTest extends AppCompatActivity {

    /**
     * Actions and extras should be identified with the fully qualified action ID since
     * messages are sent system-wide
     *
     * NOTE: The exact IDS for lap counting are subject to change, and they'll be scoped to
     * a different class anyway.
     */
    public static final String ACTION_LAP_COUNT =
            "edu.drexel.lapcounter.lapcounter.ACTION_LAP_COUNT";
    public static final String EXTRA_CURRENT_LAP_COUNT =
            "edu.drexel.lapcounter.lapcounter.ACTION_LAP_COUNT";

    /**
     * This class reduces the boilerplate of subscribing to custom events.
     *
     * It is probably best if these objects are always owned by the Context
     * (in this case the Activity, but Services also are Contexts)
     */
    private SimpleMessageReceiver mReceiver = new SimpleMessageReceiver();

    /**
     * Callback for parsing a message. This is an example of what a message callback
     * should
     */
    private SimpleMessageReceiver.MessageHandler updateUI = new SimpleMessageReceiver.MessageHandler() {
        @Override
        public void onMessage(Intent message) {
            // Extract info from the Intent
            int lapCount = message.getIntExtra(EXTRA_CURRENT_LAP_COUNT, 0);


            //Update the TextView
            mCounter.setText(Integer.toString(lapCount));
        }
    };

    // Text fields in the activity
    private TextView mCounter;

    // Object for scheduling fake laps counted.
    private Handler mHandler = new Handler();
    private int mFakeLapCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receiver_test);

        mCounter = findViewById(R.id.lapCount);
        mCounter.setText("0");

        // Register callbacks with the receiver
        mReceiver.registerHandler(ACTION_LAP_COUNT, updateUI);

        simulateLaps();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Enable the callbacks by registering the receiver
        // This is shorthand for registerReceiver()
        mReceiver.attach(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Unsubscribe from events
        // This is shorthand for unregisterReceiver()
        mReceiver.detach(this);
    }

    /**
     * Simulate counting laps. This just increments the counter once a second
     */
    private void simulateLaps() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mFakeLapCount += 2;

                publishLapCount();
                simulateLaps();
            }
        }, 1000);
    }

    /**
     * This method simulates publishing an event from the LapCounter component of the backend. This
     * gives an example of how to send custom messages. Simona, you'll need to do something like
     * this until Royce and I finish the backend branch.
     */
    private void publishLapCount() {
        // Create the intent, specifying the name of the event (i.e. "action")
        Intent intent = new Intent(ACTION_LAP_COUNT);

        // If needed, store some key-value pairs in the intent
        intent.putExtra(EXTRA_CURRENT_LAP_COUNT, mFakeLapCount);

        // Finally, publish the event. As long as you have a Context (Activity/Service/etc) you
        // can broadcast intents
        Helper.sendBroadcast(this, intent);
    }
}
