package edu.drexel.lapcounter.lapcounter.frontend;


import android.app.DatePickerDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.arch.persistence.room.Room;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;


import java.util.ArrayList;
import java.util.Calendar;

import java.util.List;

import edu.drexel.lapcounter.lapcounter.R;
import edu.drexel.lapcounter.lapcounter.backend.TimestampConverter;
import edu.drexel.lapcounter.lapcounter.backend.Database.WorkoutViewModel;
import edu.drexel.lapcounter.lapcounter.backend.Database.Workouts;
import edu.drexel.lapcounter.lapcounter.frontend.navigationbar.NavBar;
import edu.drexel.lapcounter.lapcounter.backend.Database.WorkoutDatabase;



// In the final version, use R.string.<string id> for titles

public class WorkoutHistoryActivity extends AppCompatActivity {

    //
    //WORKOUT DETAILS ACTIVITY INFO
    //
    private WorkoutViewModel mWorkoutViewModel;
    private final static int MILLISECONDS_IN_SECOND = 1000;

    //
    //DATABASE VARIABLES
    //
//    public static WorkoutDatabase WorkoutDatabase;


    List <Workouts> workoutsBetweenDateRange;
    int id = 10;
    Workouts workout = new Workouts();


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

    private String tempMonth;
    private String tempDay;

    //
    //
    BarChart barChart;
    ArrayList<String> dates;
    ArrayList<BarEntry> barEntries;


    //
    //TESTER VARIABLES
    //
    private Date dbStartDate;
    private Date dbEndDate;


    private String chartStartDate;
    private String chartEndDate;


//    private int count;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_history);

        mWorkoutViewModel = ViewModelProviders.of(this).get(WorkoutViewModel.class);




        //
        //NAV BAR  onCreate
        //
        getSupportActionBar().setTitle("Workout History");

        mNavBar.init();


        //
        //NEW DATABASE LAYOUT
        //



//        workout.setID(10);
//        workout.setPoolLength(25);
//        workout.setTotalDistanceTraveled(1200);
//        workout.setStartDateTime(TimestampConverter.fromTimestamp("2018-4-25 12:00:00.000"));
//        workout.setEndDateTime(TimestampConverter.fromTimestamp("2018-4-25 14:00:00.000"));
//        workout.setLaps(34);
//        workout.setPoolUnits("Yards");
//        mWorkoutViewModel.insert(workout);
        workout = mWorkoutViewModel.getWorkoutByID(id);





        setAvgWorkoutTimeText = findViewById(R.id.AVGWT_Data);
        setTotalDistanceTraveledText = findViewById(R.id.TDT_Data);
        setAvgWorkoutDistanceText = findViewById(R.id.AVG_WD_Data);
        setTimeDurationText = findViewById(R.id.DUR_Data);


        //
        //
        //END OF DATABASE PORTION
        //
        //



//region implements View.OnClickListener



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

                if (month <10){
                    tempMonth = "0"+ String.valueOf(month);
                }
                else {
                    tempMonth= String.valueOf(month);
                }

                if (dayOfMonth <10){
                    tempDay = "0"+ String.valueOf(dayOfMonth);
                }
                else {
                    tempDay= String.valueOf(dayOfMonth);
                }



                chartStartDate = tempMonth + "/" + tempDay + "/" + year;
                Log.d(TAG,"DateStartListener: "+chartStartDate);

                dbStartDate= TimestampConverter.fromTimestamp(year + "-" + tempMonth +  "-" + tempDay+ " 00:00:00.000");
                DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
                df.setTimeZone(TimeZone.getTimeZone("UTC"));

                Log.d(TAG,"DateStartListener: "+dbStartDate);
//                dbStartDate = String.valueOf(year)+tempMonth+tempDay;

                Workout_History_Start_Date.setText(chartStartDate);
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

                if (month <10){
                    tempMonth = "0"+ String.valueOf(month);
                }
                else {
                    tempMonth= String.valueOf(month);
                }

                if (dayOfMonth <10){
                    tempDay = "0"+ String.valueOf(dayOfMonth);
                }
                else {
                    tempDay= String.valueOf(dayOfMonth);
                }


                //
                //TESTING END DATE
                //
                chartEndDate = tempMonth + "/" + tempDay + "/" + year;

                dbEndDate= TimestampConverter.fromTimestamp( year + "-" + tempMonth +  "-" + tempDay+ " 00:00:00.000");
//               dbEndDate = String.valueOf(year)+tempMonth+tempDay;

                Workout_History_End_Date.setText(chartEndDate);
                DateChecker();
            }
        };

        //
        //END OF End_Date Picker
        //

//endregion implements View.OnClickListener
    }




    public void DateChecker(){


        if ((!Workout_History_Start_Date.getText().toString().equals("Start Date"))&&(!Workout_History_End_Date.getText().toString().equals("End Date")))
        {
                workout = mWorkoutViewModel.getWorkoutByID(id);
//            workoutsBetweenDateRange =  mWorkoutViewModel.findWorkoutsBetweenDates(dbStartDate,dbEndDate);
//            if (workoutsBetweenDateRange.size() < 0) {
//                createBarGraph(chartStartDate,chartEndDate);

                valueCalulations(workout);
//            }
        }
    }

    public void valueCalulations(Workouts Workout){
        int TotalDistance = 0;
        int AVGWORKDIST;
        long difference=0;
        String unit_abbrev = ((workout.getPoolUnits().compareTo("Meters") == 0) ? " m": " yd");
                DateFormat df = new SimpleDateFormat("MM/dd/yyyy");

//        for (Workouts wrkout : workoutsBetweenDateRange) {

           TotalDistance += Workout.getTotalDistanceTraveled();
           difference += Workout.getEndDateTime().getTime() - workout.getStartDateTime().getTime();
//        }

        //
        //TOTAL DISTANCE TRAVELED
        //

        String distance = Integer.toString(TotalDistance) + unit_abbrev;
        setTotalDistanceTraveledText.setText(distance);


        //
        //AVERAGE WORKOUT DISTANCE
        //

        AVGWORKDIST = TotalDistance;
        String averageDistance = Integer.toString(AVGWORKDIST) + unit_abbrev;

        setAvgWorkoutDistanceText.setText(averageDistance);

        //
        //DURATION
        //

        Date d = new Date(difference);
        df = new SimpleDateFormat("HH:mm:ss");
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        String time_formatted = df.format(d);
        setTimeDurationText.setText(time_formatted);

        //
        //AVERAGE WORKOUT TIME
        //

        float avg_lap_time = ((float)difference / Float.valueOf(1))/MILLISECONDS_IN_SECOND;
        String avg_time_str = String.format("%.2f sec",avg_lap_time);
        setAvgWorkoutTimeText.setText(avg_time_str);

    }

    public void createBarGraph(String Date1, String Date2){

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy");
//        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd");
        int count;
        int totaldistance;

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


            String tempStartDate ;

            List<Workouts> workouts = mWorkoutViewModel.findWorkoutsBetweenDates(dbStartDate, dbEndDate);

            for(int j = 0; j< dates.size();j++) {
                count =0;
                totaldistance = 0;

                for(int i =0; i<workouts.size()-1;i++) {


                    tempStartDate = String.valueOf(workouts.get(i).getStartDateTime());
                    tempStartDate =  tempStartDate.substring(4, 6) + "/" +tempStartDate.substring(6, 8) + "/" + tempStartDate.substring(0, 4) ;


                    if (dates.get(j).equals(tempStartDate)) {

                        totaldistance += workouts.get(i).getTotalDistanceTraveled();
                        count++;
                    }
                }

                if (count == 0) {
                    count = 1;
                }

                totaldistance = totaldistance / count;

                barEntries.add(new BarEntry(totaldistance, j));

            }

        }catch(ParseException e){
            e.printStackTrace();
        }

        BarDataSet barDataSet = new BarDataSet(barEntries,"Dates");
        BarData barData = new BarData(dates,barDataSet);
        barChart.setData(barData);
        barChart.setDescription("");
        barChart.notifyDataSetChanged();

    }




    public ArrayList<String> getList(Calendar  startDate, Calendar  endDate){
        ArrayList<String> list = new ArrayList<String>();
        while(startDate.compareTo(endDate)<=0){
            list.add(getDate(startDate));

            startDate.add(Calendar.DAY_OF_MONTH,1);
        }
        return list;
    }

    public String getDate(Calendar cld){
        String curDate =  (cld.get(Calendar.MONTH) + 1)+ "/" +cld.get(Calendar.DAY_OF_MONTH)  + "/"
                +cld.get(Calendar.YEAR);
        try{
            Date date = new SimpleDateFormat("MM/dd/yyyy").parse(curDate);
            curDate =  new SimpleDateFormat("MM/dd/yyyy").format(date);
        }catch(ParseException e){
            e.printStackTrace();
        }
        return curDate;
    }



}