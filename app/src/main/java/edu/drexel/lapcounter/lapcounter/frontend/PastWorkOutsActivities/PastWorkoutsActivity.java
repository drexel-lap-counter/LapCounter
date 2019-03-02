package edu.drexel.lapcounter.lapcounter.frontend.PastWorkOutsActivities;



import android.arch.persistence.room.Room;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import edu.drexel.lapcounter.lapcounter.R;
import edu.drexel.lapcounter.lapcounter.backend.Database.WorkoutDatabase;
import edu.drexel.lapcounter.lapcounter.backend.Database.Workouts;
import edu.drexel.lapcounter.lapcounter.frontend.CurrentWorkoutActivity;
import edu.drexel.lapcounter.lapcounter.frontend.navigationbar.NavBar;

public class PastWorkoutsActivity extends AppCompatActivity {
    private final NavBar mNavBar = new NavBar(this, R.id.navigation_past_workouts);
    private String tempStartDate;

    public static WorkoutDatabase WorkoutDatabase;



    private static final String TAG = "PastWorkoutsActivity";


    private ArrayList<String> mWorkoutDate = new ArrayList<>();
    private ArrayList<String> mPoolLength = new ArrayList<>();
    //
    public static final int CURRENT_ACTIVITY_REQUEST_CODE= 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {


        WorkoutDatabase = Room.databaseBuilder(getApplicationContext(), WorkoutDatabase.class, "workoutsdb").allowMainThreadQueries().build();

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


        initPastWorkoutsView();
        mNavBar.init();
    }

    private void initPastWorkoutsView() {

        List<Workouts> workouts = WorkoutDatabase.workoutDao().getAllWorkouts();
        for (Workouts wrkout : workouts) {

            tempStartDate = String.valueOf(wrkout.getStartDate());
            tempStartDate =  tempStartDate.substring(4, 6) + "/" +tempStartDate.substring(6, 8) + "/" + tempStartDate.substring(0, 4);

            mWorkoutDate.add(String.valueOf(tempStartDate));
            mPoolLength.add(String.valueOf(wrkout.getPoolLength()));

    }

        initRecyclerView();
    }

    private void initRecyclerView(){

        RecyclerView recyclerView = findViewById(R.id.recyclerv_view);
        RecyclerViewAdapter adapter = new RecyclerViewAdapter(this, mWorkoutDate,mPoolLength);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

    }
//
//    public void selectWorkout(View view) {
//        Intent intent = new Intent(this, WorkoutDetailsActivity.class);
//        startActivity(intent);
//    }
}
