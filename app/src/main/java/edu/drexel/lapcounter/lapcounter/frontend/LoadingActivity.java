package edu.drexel.lapcounter.lapcounter.frontend;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import edu.drexel.lapcounter.lapcounter.R;
import edu.drexel.lapcounter.lapcounter.backend.Database.Device.DeviceRepository;
import edu.drexel.lapcounter.lapcounter.backend.Database.State.State;
import edu.drexel.lapcounter.lapcounter.backend.Database.Transition.TransitionRepository;
import edu.drexel.lapcounter.lapcounter.backend.Database.Units.Units;
import edu.drexel.lapcounter.lapcounter.backend.Database.Workout.Workout;
import edu.drexel.lapcounter.lapcounter.backend.Database.Workout.WorkoutRepository;
import edu.drexel.lapcounter.lapcounter.backend.Database.Workout.WorkoutViewModel;
import edu.drexel.lapcounter.lapcounter.backend.TimestampConverter;

public class LoadingActivity extends AppCompatActivity {

    /*** final static strings for units ***/
    public final static String unitsMeters = "Meters";
    public final static String unitsYards = "Yards";

    /*** final static string for states ***/
    public final static String statesNear = "NEAR";
    public final static String statesFar = "FAR";
    public final static String statesUnknown = "UNKNOWN";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading_screen);

        // In the final version, use R.string.<string id> for titles
        getSupportActionBar().setTitle("Splash Screen");
        WorkoutViewModel mWorkoutViewModel =  ViewModelProviders.of(this).get(WorkoutViewModel.class);


        try
        {
            //Units setup
            List<Units> units = new ArrayList<>();
            List<State> states = new ArrayList<>();
            units = mWorkoutViewModel.getAllUnits();
            if(units.size() == 0)
            {
                Units unit = new Units(unitsMeters);
                mWorkoutViewModel.insert(unit);
                unit = new Units(unitsYards);
                mWorkoutViewModel.insert(unit);
            }

            TransitionRepository repo = new TransitionRepository(getApplication());
            states = repo.getAllStates();
            if(states.size() == 0)
            {
                State state = new State(statesFar);
                repo.insert(state);
                state = new State(statesNear);
                repo.insert(state);
                state = new State(statesUnknown);
                repo.insert(state);
            }
        }
        catch(Exception e)
        {
            //Not sure what to do here
            Log.e("LoadingActivity","Error Connecting and Initalizing Database");
        }



        //Workout example data setup
        List<Workout> workouts = mWorkoutViewModel.getAllWorkouts();
        //Debug
        if(workouts.size() == 0)
        {
            Workout workout= new Workout();

            workout.setID(10);
            workout.setPoolLength(25);
            workout.setTotalDistanceTraveled(1200);
            workout.setStartDate(TimestampConverter.fromTimestamp(1554904800));
            workout.setEndDate(TimestampConverter.fromTimestamp(1554912000));
            workout.setLaps(34);
            workout.setPoolUnits(unitsMeters);
            mWorkoutViewModel.insert(workout);

            workout = new Workout();
            workout.setPoolLength(50);
            workout.setTotalDistanceTraveled(500);
            workout.setStartDate(TimestampConverter.fromTimestamp(1554976800));
            workout.setEndDate(TimestampConverter.fromTimestamp(1554984000));
            workout.setLaps(10);
            workout.setPoolUnits(unitsYards);
            mWorkoutViewModel.insert(workout);
        }



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
