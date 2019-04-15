package edu.drexel.lapcounter.lapcounter.frontend;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

import edu.drexel.lapcounter.lapcounter.R;
import edu.drexel.lapcounter.lapcounter.backend.Database.Device.Device;
import edu.drexel.lapcounter.lapcounter.backend.Database.Device.DeviceViewModel;
import edu.drexel.lapcounter.lapcounter.backend.TimestampConverter;
import edu.drexel.lapcounter.lapcounter.backend.Database.Workout.WorkoutViewModel;
import edu.drexel.lapcounter.lapcounter.backend.Database.Workout.Workout;
import edu.drexel.lapcounter.lapcounter.frontend.navigationbar.NavBar;

public class WorkoutDetailsActivity extends AppCompatActivity {


    private final NavBar mNavBar = new NavBar(this);
    private WorkoutViewModel mWorkoutViewModel;
    private DeviceViewModel mDeviceViewModel;
    private int mWorkout_id;
    private final static int MILLISECONDS_IN_SECOND = 1000;

    private static String qualify(String s) {
        return WorkoutDetailsActivity.class.getPackage().getName() + "." + s;
    }
    public static final String EXTRAS_WORKOUT_ID = qualify("DEVICE_ADDRESS");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_details);

        Intent intent = getIntent();
        mWorkout_id = intent.getIntExtra(EXTRAS_WORKOUT_ID, -1);

        // In the final version, use R.string.<string id> for titles
        getSupportActionBar().setTitle("Workout Details");

        mNavBar.init();

        mWorkoutViewModel = ViewModelProviders.of(this).get(WorkoutViewModel.class);
        Workout workout = new Workout();

        mDeviceViewModel = ViewModelProviders.of(this).get(DeviceViewModel.class);
        Device d = new Device("Test_Device","00:11:00:33:AA",-25);



        mDeviceViewModel.insert(d);
        //This code maintains a known entry in the DB for testing, the insert will update the
        //workout with ID 10 in the DB.

//        workout.setID(10);
//        workout.setPoolLength(25);
//        workout.setTotalDistanceTraveled(1200);
//        workout.setStartDate(TimestampConverter.fromTimestamp(1554904800));
//        workout.setEndDate(TimestampConverter.fromTimestamp(1554912000));
//        workout.setDeviceMAC(d.getMacAddress());
//        workout.setLaps(34);
//        workout.setPoolUnits("Meters");
//        mWorkoutViewModel.insert(workout);

        //Below code is commented so Neil has an example of how the new viewmodels and their functions work.
        //If you comment below lines, it will insert a new workout with the next available unused ID number.
        //getAllWorkouts and getWorkoutsByDateRange are also below to show how they work, and that they do work.
        //All this can be deleted at a later date

//        Workout a_workout = new Workout();
//        a_workout.setPoolLength(50);
//        a_workout.setTotalDistanceTraveled(500);
//        a_workout.setStartDate(TimestampConverter.fromTimestamp(1523440800));
//        a_workout.setEndDate(TimestampConverter.fromTimestamp(1523448000));
//        a_workout.setDeviceMAC(d.getMacAddress());
//        a_workout.setLaps(10);
//        a_workout.setPoolUnits("Meters");
//        mWorkoutViewModel.insert(a_workout);


//        ArrayList<Workout> test = mWorkoutViewModel.getAllWorkouts();
//        ArrayList<Workout> test2 = mWorkoutViewModel.getWorkoutsByDateRange(TimestampConverter.fromTimestamp(1554768000),TimestampConverter.fromTimestamp(1555096321));
//

        workout = mWorkoutViewModel.getWorkoutByID(mWorkout_id);
        DisplayWorkoutDetails(workout);
    }

    private void DisplayWorkoutDetails(Workout workout)
    {
        String unit_abbrev = ((workout.getPoolUnits().compareTo("Meters") == 0) ? " m": " yd");

        TextView date_view = findViewById(R.id.dateOfWorkoutTextView);
        TextView pool_length_view = findViewById(R.id.poolLengthValueTextView);
        TextView workout_duration_view = findViewById(R.id.workoutDurationValueTextView);
        TextView average_lap_time_view = findViewById(R.id.avgLapTimeValueTextView);
        TextView average_speed_view = findViewById(R.id.averageSpeedValueTextView);
        TextView laps_view = findViewById(R.id.lapsValueTextView);
        TextView total_distance_view = findViewById(R.id.totalDistanceValueTextView);

        //Date of Workout
        DateFormat df = new SimpleDateFormat("dd MMM yyyy");
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        String output = df.format(workout.getStartDate());
        date_view.setText(output);

        //Pool Length
        String pool_length = Integer.toString(workout.getPoolLength())+ unit_abbrev;

        pool_length_view.setText(pool_length);

        //Workout Duration
        long difference = workout.getEndDate().getTime() - workout.getStartDate().getTime();
        Date d = new Date(difference);
        df = new SimpleDateFormat("HH:mm:ss");
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        String time_formatted = df.format(d);
        workout_duration_view.setText(time_formatted);

        //Avg Lap Time
        float avg_lap_time = ((float)difference / Float.valueOf(workout.getLaps()))/MILLISECONDS_IN_SECOND;
        String avg_time_str = String.format("%.2f sec",avg_lap_time);
        average_lap_time_view.setText(avg_time_str);

        //Average Speed
        float speed = (float)workout.getPoolLength()/avg_lap_time;
        String average_speed = String.format("%.2f %s/sec",speed,unit_abbrev);

        average_speed_view.setText(average_speed);

        //Lap Count
        String laps = Integer.toString(workout.getLaps());
        laps_view.setText(laps);

        //Total Distance
        String distance = Integer.toString(workout.getTotalDistanceTraveled()) + unit_abbrev;
        total_distance_view.setText(distance);
    }
}
