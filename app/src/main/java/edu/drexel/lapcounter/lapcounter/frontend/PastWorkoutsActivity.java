package edu.drexel.lapcounter.lapcounter.frontend;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.TimeZone;

import edu.drexel.lapcounter.lapcounter.R;
import edu.drexel.lapcounter.lapcounter.backend.Database.WorkoutViewModel;
import edu.drexel.lapcounter.lapcounter.backend.Database.Workouts;
import edu.drexel.lapcounter.lapcounter.backend.TimestampConverter;
import edu.drexel.lapcounter.lapcounter.frontend.navigationbar.NavBar;

public class PastWorkoutsActivity extends AppCompatActivity {
    private final NavBar mNavBar = new NavBar(this, R.id.navigation_past_workouts);
    private String tempStartDate;

    private WorkoutViewModel mWorkoutViewModel;

    int id = 10;

    private static final String TAG = "PastWorkoutsActivity";



    private ArrayList<String> mWorkoutDate = new ArrayList<>();
    private ArrayList<String> mPoolLength = new ArrayList<>();
    private ArrayList<String> mID = new ArrayList<>();
    //
    public static final int CURRENT_ACTIVITY_REQUEST_CODE= 1;



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        mWorkoutViewModel = ViewModelProviders.of(this).get(WorkoutViewModel.class);

        Workouts workout = new Workouts();


//        workout.setID(10);
//        workout.setPoolLength(25);
//        workout.setTotalDistanceTraveled(1200);
//        workout.setStartDateTime(TimestampConverter.fromTimestamp("2018-4-25 12:00:00.000"));
//        workout.setEndDateTime(TimestampConverter.fromTimestamp("2018-4-25 14:00:00.000"));
//        workout.setLaps(34);
//        workout.setPoolUnits("Yards");
//        mWorkoutViewModel.insert(workout);


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_past_workouts);
        Log.d(TAG, "onCreate: started.");
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(PastWorkoutsActivity.this, CurrentWorkoutActivity.class);
                startActivityForResult(intent, CURRENT_ACTIVITY_REQUEST_CODE);
            }
        });

        workout = mWorkoutViewModel.getWorkoutByID(id);
        initPastWorkoutsView(workout);
        initRecyclerView();
        mNavBar.init();

    }

    private void initPastWorkoutsView(Workouts Workout) {

//        for (Workouts wrkout : workouts) {
        DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        tempStartDate = df.format(Workout.getStartDateTime());

//            tempStartDate = String.valueOf(Workout.getStartDateTime());
            tempStartDate =  tempStartDate.substring(0,10);
            mWorkoutDate.add(String.valueOf(tempStartDate));
            mPoolLength.add(String.valueOf(Workout.getPoolLength()));
            mID.add(String.valueOf(Workout.getID()));

//        }
    }

    private void initRecyclerView(){

        RecyclerView recyclerView = findViewById(R.id.recyclerv_view);
        RecyclerViewAdapter adapter = new RecyclerViewAdapter(this, mWorkoutDate,mPoolLength,mID);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

    }
}
