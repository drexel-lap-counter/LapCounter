package edu.drexel.lapcounter.lapcounter.frontend.navigationbar;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.lang.ref.WeakReference;

import edu.drexel.lapcounter.lapcounter.R;
import edu.drexel.lapcounter.lapcounter.frontend.CurrentWorkoutActivity;
import edu.drexel.lapcounter.lapcounter.frontend.PastWorkoutsActivity;
import edu.drexel.lapcounter.lapcounter.frontend.SettingsActivity;
import edu.drexel.lapcounter.lapcounter.frontend.WorkoutHistoryActivity;

public class NavBar implements BottomNavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = NavBar.class.getSimpleName();
    private static final int TOGGLE_NONE = -1;

    /**
     * A reference to the parent activity that owns this Listener object.
     * <p>
     *     We use a weak reference to break any reference cycle between the parent activity and this
     *     object.
     * </p>
     */
    private WeakReference<AppCompatActivity> mParent;

    /**
     * Which menu option should appear toggled in the UI.
     * <p>
     * IDs can be found in res/menu/navigation.xml
     * If this field is TOGGLE_NONE, then no items will be toggled.
     * </p>
     */
    private int mMenuOptionIdToToggle;

    /**
     * Constructor for activities reached directly from the navbar
     * @param parent
     * @param menuOptionIdToToggle
     */
    public NavBar(AppCompatActivity parent, int menuOptionIdToToggle) {
        mParent = new WeakReference<>(parent);
        mMenuOptionIdToToggle = menuOptionIdToToggle;
    }

    /**
     * Constructor for activities not reached directly from the navbar
     * @param parent
     */
    public NavBar(AppCompatActivity parent) {
        this(parent, TOGGLE_NONE);
    }

    /**
     * Attach eventhandlers for the menu bar, and toggle the checked state for the menu option
     * with id == mMenuOptionIdToToggle.
     */
    public void init() {
        BottomNavigationView view = getBottomNavigationView();

        if (view == null) {
            return;
        }

        view.setOnNavigationItemSelectedListener(this);
        adjustToggledStates(view);
    }

    /**
     * a helper method for init
     * @param view
     */
    private void adjustToggledStates(BottomNavigationView view) {
        Menu menu = view.getMenu();

        // Make all items appear uncheckable.
        // This unchecks the default menu item.
        menu.setGroupCheckable(0, false, true);

        if (mMenuOptionIdToToggle == TOGGLE_NONE) {
            return;
        }

        // Make all items appear checkable again.
        menu.setGroupCheckable(0, true, true);

        MenuItem itemToToggle = menu.findItem(mMenuOptionIdToToggle);

        if (itemToToggle == null) {
            Log.e(TAG, String.format("%d is not a valid menu option id.", mMenuOptionIdToToggle));
            return;
        }

        itemToToggle.setChecked(true);
    }

    /**
     * @return BottomNavigationView
     */
    private BottomNavigationView getBottomNavigationView() {
        AppCompatActivity parent = getParent("getBottomNavigationView");

        if (parent == null) {
            return null;
        }

        BottomNavigationView view = parent.findViewById(R.id.navigation);

        if (view == null) {
            Log.e(TAG, "getBottomNavigationView() - View not found.");
        }

        return view;
    }

    /**
     * provides error handling when retrieving the context
     * @param description
     * @return AppCompatActivity
     */
    private AppCompatActivity getParent(String description) {
        AppCompatActivity context = mParent.get();

        if (context == null) {
            String errMsg = String.format("When attempting to %s: parent activity has been " +
                                          "finalized.", description);
            Log.e(TAG, errMsg);
        }

        return context;
    }

    /**
     * method to handle menu selections
     * @param menuItem
     * @return boolean
     */
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        Context context = getParent("onNavigationItemSelected");

        if (context == null) {
            return false;
        }

        Class activity;

        switch (menuItem.getItemId()) {
            case R.id.navigation_home:
                activity = CurrentWorkoutActivity.class;
                break;
            case R.id.navigation_analytics:
                activity = WorkoutHistoryActivity.class;
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
