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
import java.util.List;
import java.util.TimeZone;

import edu.drexel.lapcounter.lapcounter.R;
import edu.drexel.lapcounter.lapcounter.backend.Database.Workout.WorkoutViewModel;
import edu.drexel.lapcounter.lapcounter.backend.Database.Workout.Workout;
import edu.drexel.lapcounter.lapcounter.frontend.navigationbar.NavBar;

public class PastWorkoutsActivity extends AppCompatActivity {
    private final NavBar mNavBar = new NavBar(this, R.id.navigation_past_workouts);
    private String tempStartDate;

    private WorkoutViewModel mWorkoutViewModel;



    private static final String TAG = "PastWorkoutsActivity";

    List<Workout> allWorkoutsDesc;
    private ArrayList<String> mWorkoutDate = new ArrayList<>();
    private ArrayList<String> mPoolLength = new ArrayList<>();
    private ArrayList<String> mID = new ArrayList<>();






    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mWorkoutViewModel = ViewModelProviders.of(this).get(WorkoutViewModel.class);


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_past_workouts);
        Log.d(TAG, "onCreate: started.");
        // In the final version, use R.string.<string id> for titles
        getSupportActionBar().setTitle("Past Workouts");


        try {
            allWorkoutsDesc =  mWorkoutViewModel.getAllWorkoutsDecending();
        } catch (Exception e) {
            e.printStackTrace();
        }


        for (Workout wrkout : allWorkoutsDesc) {
            initPastWorkoutsView(wrkout);
        }


        initRecyclerView();
        mNavBar.init();
    }


    private void initRecyclerView(){

        RecyclerView recyclerView = findViewById(R.id.recyclerv_view);
        PastWorkoutsRecyclerAdapter adapter = new PastWorkoutsRecyclerAdapter(this, mWorkoutDate,mPoolLength,mID);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

    }


    private void initPastWorkoutsView(Workout Workout) {




        DateFormat df = new SimpleDateFormat("dd MMM yyyy");


        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        String output = df.format(Workout.getStartDate());


        tempStartDate =  String.valueOf(output);
        mWorkoutDate.add(String.valueOf(tempStartDate));
        mPoolLength.add(String.valueOf(Workout.getPoolLength()));
        mID.add(String.valueOf(Workout.getID()));


    }


}
