package edu.drexel.lapcounter.lapcounter.frontend.navigationbar;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;

import java.lang.ref.WeakReference;

import edu.drexel.lapcounter.lapcounter.R;
import edu.drexel.lapcounter.lapcounter.frontend.PastWorkoutsActivity;
import edu.drexel.lapcounter.lapcounter.frontend.SettingsActivity;
import edu.drexel.lapcounter.lapcounter.frontend.WelcomeActivity;
import edu.drexel.lapcounter.lapcounter.frontend.WorkoutDetailsActivity;

public class OnItemSelectedListener implements BottomNavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = OnItemSelectedListener.class.getSimpleName();

    // A reference to the parent activity that owns this Listener object.
    // We use a weak reference to break any reference cycle between the parent activity and this
    // object.
    private WeakReference<AppCompatActivity> mParent;

    public OnItemSelectedListener(AppCompatActivity parent) {
        mParent = new WeakReference<>(parent);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        AppCompatActivity context = mParent.get();

        if (context == null) {
            Log.e(TAG, "Parent activity has been finalized.");
            return false;
        }

        Class activity;

        switch (menuItem.getItemId()) {
            case R.id.navigation_home:
                activity = WelcomeActivity.class;
                break;
            case R.id.navigation_analytics:
                activity = WorkoutDetailsActivity.class;
                break;
            case R.id.navigation_past_workouts:
                activity = PastWorkoutsActivity.class;
                break;
            case R.id.navigation_settings:
                activity = SettingsActivity.class;
                break;
            default:
                return false;
        }

        // Start a new activity or bring a previous instance to the front of the back stack.
        Intent intent = new Intent(context, activity);

        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        context.startActivity(intent);

        return false;
    }
}
