package edu.drexel.lapcounter.lapcounter.frontend;


import android.app.DatePickerDialog;
import android.arch.persistence.room.Room;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;


import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;


import edu.drexel.lapcounter.lapcounter.R;
import edu.drexel.lapcounter.lapcounter.backend.Database.WorkoutDatabase;
import edu.drexel.lapcounter.lapcounter.backend.Database.Workouts;
import edu.drexel.lapcounter.lapcounter.frontend.navigationbar.NavBar;


// In the final version, use R.string.<string id> for titles

public class WorkoutHistoryActivity extends AppCompatActivity {


    //
    //DATABASE VARIABLES
    //
    public static WorkoutDatabase WorkoutDatabase;

    private TextView setTotalDistanceTraveledText;
    private TextView setAvgWorkoutDistanceText;
    private TextView setTimeDurationText;
    private TextView setAvgWorkoutTimeText;


    //
    //NAV BAR VARIABLES
    //

    private final NavBar mNavBar = new NavBar(this, R.id.navigation_analytics);
    private static final String TAG = "WorkoutHistoryActivity";

    //
    //Date Picker Variables
    //



    private TextView Workout_History_Start_Date;
    private TextView Workout_History_End_Date;


    private DatePickerDialog.OnDateSetListener DateSetListener_Start;
    private DatePickerDialog.OnDateSetListener DateSetListener_End;

    //
    //
    //
    BarChart barChart;
    ArrayList<String> dates;
    Random random;
    ArrayList<BarEntry> barEntries;


    //
    //TESTER VARIABLES
    //
    private String startDate;
    private String endDate;

    private int count;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_history);
        //
        //NAV BAR  onCreate
        //
        getSupportActionBar().setTitle("Workout History");

        mNavBar.init();

        //
        //END OF NAVBAR
        //

        //
        //Chart Stuff
        //
        barChart = (BarChart) findViewById(R.id.bargraph);

        createRandomBarGraph("2016/05/05", "2016/06/01");



        //
        //DATABASE onCreate Stuff
        //

        WorkoutDatabase = Room.databaseBuilder(getApplicationContext(), WorkoutDatabase.class, "workoutsdb").allowMainThreadQueries().build();
        WorkoutDatabase.workoutDao().deleteAll();

        //
        //TEST DATA
        //
        Workouts workout = new Workouts();
        workout.setmStartDate(20190215);
        workout.setEndDate(20190215);
        workout.setPoolLength(50);
        workout.setTotalDistanceTraveled(100);
        workout.setAvgWorkoutDistance(100);
        WorkoutDatabase.workoutDao().addWorkout(workout);

        workout.setmStartDate(20190216);
        workout.setEndDate(20190216);
        workout.setPoolLength(25);
        workout.setTotalDistanceTraveled(75);
        workout.setAvgWorkoutDistance(75);
        WorkoutDatabase.workoutDao().addWorkout(workout);


        workout.setmStartDate(20190220);
        workout.setEndDate(20190220);
        workout.setPoolLength(25);
        workout.setTotalDistanceTraveled(100);
        workout.setAvgWorkoutDistance(100);
        WorkoutDatabase.workoutDao().addWorkout(workout);

        workout.setmStartDate(20190221);
        workout.setEndDate(20190221);
        workout.setPoolLength(50);
        workout.setTotalDistanceTraveled(200);
        workout.setAvgWorkoutDistance(200);
        WorkoutDatabase.workoutDao().addWorkout(workout);

        workout.setmStartDate(20190222);
        workout.setEndDate(20190222);
        workout.setPoolLength(25);
        workout.setTotalDistanceTraveled(100);
        workout.setAvgWorkoutDistance(100);
        WorkoutDatabase.workoutDao().addWorkout(workout);


        setTotalDistanceTraveledText = findViewById(R.id.TDT_Data);
        setAvgWorkoutDistanceText = findViewById(R.id.AVG_WD_Data);



        //
        //
        //END OF DATABASE PORTION
        //
        //






        //
        //DATE SELECTION RANGE onCreate Stuff
        //
        Workout_History_Start_Date = (TextView) findViewById(R.id.Workout_History_Start_Date);
        Workout_History_End_Date = (TextView) findViewById(R.id.Workout_History_End_Date);


        //
        //START_Date Picker
        //

        Workout_History_Start_Date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int day = cal.get(Calendar.DAY_OF_MONTH);



                DatePickerDialog dialog = new DatePickerDialog(WorkoutHistoryActivity.this, android.R.style.Theme_Holo_Light_Dialog_MinWidth, DateSetListener_Start, year, month, day);

                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();

            }
        });



        DateSetListener_Start = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                month = month + 1;


                Log.d(TAG, "onDateSet: Date: mm/dd/yyyy" + month + "/" + dayOfMonth + "/" + year);


                    String date = month + "/" + dayOfMonth + "/" + year;

                Workout_History_Start_Date.setText(date);



                //
                //TESTING STARTDATE
                //

                if (month <10){
                    startDate = String.valueOf(year)+"0"+String.valueOf(month)+String.valueOf(dayOfMonth);
                }
                else {
                    startDate = String.valueOf(year) + String.valueOf(month) + String.valueOf(dayOfMonth);
                }
//                startDate = String.valueOf(year)+String.valueOf(month)+String.valueOf(dayOfMonth);

                setTimeDurationText = findViewById(R.id.DUR_Data);
                setTimeDurationText.setText(String.valueOf(startDate));

                DateChecker();
            }
        };

        //
        //End Of START_Date Picker
        //



        //
        //End_Date Picker
        //

        Workout_History_End_Date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int day = cal.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog = new DatePickerDialog(WorkoutHistoryActivity.this, android.R.style.Theme_Holo_Light_Dialog_MinWidth, DateSetListener_End, year, month, day);

                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        });




        DateSetListener_End = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                month = month + 1;
                Log.d(TAG, "onDateSet: Date: mm/dd/yyyy" + month + "/" + dayOfMonth + "/" + year);
                String date = month + "/" + dayOfMonth + "/" + year;
                Workout_History_End_Date.setText(date);


                //
                //TESTING ENDDATE
                //
                if (month <10){
                    endDate = String.valueOf(year)+"0"+String.valueOf(month)+String.valueOf(dayOfMonth);
                }
                else {
                    endDate = String.valueOf(year) + String.valueOf(month) + String.valueOf(dayOfMonth);
                }
                DateChecker();
            }
        };

        //
        //END OF End_Date Picker
        //

    }




    public void DateChecker(){
        if ((!Workout_History_Start_Date.getText().toString().equals("Start Date"))&&(!Workout_History_End_Date.getText().toString().equals("End Date")))
        {
            count = WorkoutDatabase.workoutDao().findWorkoutsBetweenDates(Integer.parseInt(startDate), Integer.parseInt(endDate)).size();
            setAvgWorkoutTimeText = findViewById(R.id.AVGWT_Data);
            setAvgWorkoutTimeText.setText(String.valueOf(count));
            if (count > 0)
                valueCalulations(count);
        }
    }

    public void valueCalulations(int count){
        int TotalDistance = 0;
        int AVGWORK = 0;


        List<Workouts> workouts = WorkoutDatabase.workoutDao().getAllWorkouts();
        for (Workouts wrkout : workouts) {

            TotalDistance += wrkout.getTotalDistanceTraveled();
            AVGWORK += wrkout.getAvgWorkoutDistance();
        }

            //
            //TOTAL DISTANCE TRAVELED
            //

            setTotalDistanceTraveledText.setText(String.valueOf(TotalDistance));


            //
            //AVERAGE WORKOUT DISTANCE
            //
            AVGWORK= AVGWORK/count;
            setAvgWorkoutDistanceText.setText(String.valueOf(AVGWORK));
            //
            //AVERAGE WORKOUT
            //

            //
            //DURATION
            //

    }

    public void createRandomBarGraph(String Date1, String Date2){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd");

        try {
            Date date1 = simpleDateFormat.parse(Date1);
            Date date2 = simpleDateFormat.parse(Date2);

            Calendar mDate1 = Calendar.getInstance();
            Calendar mDate2 = Calendar.getInstance();
            mDate1.clear();
            mDate2.clear();

            mDate1.setTime(date1);
            mDate2.setTime(date2);

            dates = new ArrayList<>();
            dates = getList(mDate1,mDate2);

            barEntries = new ArrayList<>();
            float max = 0f;
            float value = 0f;
            random = new Random();
            for(int j = 0; j< dates.size();j++){
                max = 100f;
                value = random.nextFloat()*max;
                barEntries.add(new BarEntry(value,j));
            }

        }catch(ParseException e){
            e.printStackTrace();
        }

        BarDataSet barDataSet = new BarDataSet(barEntries,"Dates");
        BarData barData = new BarData(dates,barDataSet);
        barChart.setData(barData);
        barChart.setDescription("My First Bar Graph!");
    }

    public ArrayList<String> getList(Calendar startDate, Calendar endDate){
        ArrayList<String> list = new ArrayList<String>();
        while(startDate.compareTo(endDate)<=0){
            list.add(getDate(startDate));
            startDate.add(Calendar.DAY_OF_MONTH,1);
        }
        return list;
    }

    public String getDate(Calendar cld){
        String curDate = cld.get(Calendar.YEAR) + "/" + (cld.get(Calendar.MONTH) + 1) + "/"
                +cld.get(Calendar.DAY_OF_MONTH);
        try{
            Date date = new SimpleDateFormat("yyyy/MM/dd").parse(curDate);
            curDate =  new SimpleDateFormat("yyy/MM/dd").format(date);
        }catch(ParseException e){
            e.printStackTrace();
        }
        return curDate;
    }

}