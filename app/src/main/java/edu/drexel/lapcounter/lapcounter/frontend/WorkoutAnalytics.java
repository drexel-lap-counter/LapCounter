package edu.drexel.lapcounter.lapcounter.frontend;

import android.app.DatePickerDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import edu.drexel.lapcounter.lapcounter.R;
import edu.drexel.lapcounter.lapcounter.backend.Database.Workout.Workout;
import edu.drexel.lapcounter.lapcounter.backend.Database.Workout.WorkoutViewModel;
import edu.drexel.lapcounter.lapcounter.frontend.navigationbar.NavBar;

public class WorkoutAnalytics extends AppCompatActivity implements AdapterView.OnItemSelectedListener{

    //
    //NAV BAR VARIABLES
    //

    private final NavBar mNavBar = new NavBar(this, R.id.navigation_analytics);
    private static final String TAG = "WorkoutsAnalyticsActivity";


    //
    //WORKOUT ANALYTICS ACTIVITY INFO
    //
    private WorkoutViewModel mWorkoutViewModel;
    List <Workout> workoutsBetweenDateRange;
    private final static int MILLISECONDS_IN_SECOND = 1000;
    String unit_abbrev;
    private double METERTOYARDCONVERT =1.09361;
    private double YARDTOMETERCONVERT =.9144;



    //
    //DATABASE VARIABLES
    //
    private String chartStartDate;
    private String chartEndDate;
    long dbStartDate;
    long dbEndDate;

    //
    //Date Picker Variables
    //

    private TextView Workout_Analytics_Start_Date;
    private TextView Workout_Analytics_End_Date;

    private DatePickerDialog.OnDateSetListener DateSetListener_Start;
    private DatePickerDialog.OnDateSetListener DateSetListener_End;



    //
    //Chart Variables
    //
    BarChart barChart;
    ArrayList<String> dates;
    ArrayList<BarEntry> barEntries;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_analytics);
        mWorkoutViewModel = ViewModelProviders.of(this).get(WorkoutViewModel.class);

        Spinner spinner = findViewById(R.id.measurements);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,R.array.measurement_array,android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);

        Date_Picker();

        // In the final version, use R.string.<string id> for titles
        getSupportActionBar().setTitle("Workout Analytics");
        barChart = findViewById(R.id.bargraph);
        mNavBar.init();
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String text = parent.getItemAtPosition(position).toString();
        Toast.makeText(parent.getContext(),text,Toast.LENGTH_SHORT).show();
        unit_abbrev = ((text.compareTo("Meters") == 0) ? " m": " yd");
        DateChecker();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public void Date_Picker(){
        Workout_Analytics_Start_Date = (TextView) findViewById(R.id.Workout_Analytics_Start_Date);
        Workout_Analytics_End_Date = (TextView) findViewById(R.id.Workout_Analytics_End_Date);


        Workout_Analytics_Start_Date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int day = cal.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog = new DatePickerDialog(WorkoutAnalytics.this, android.R.style.Theme_Holo_Light_Dialog_MinWidth, DateSetListener_Start, year, month, day);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();

            }
        });
        DateSetListener_Start = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                chartStartDate = DateSetListener(year,month,dayOfMonth);
                Workout_Analytics_Start_Date.setText(chartStartDate);
                dbStartDate = createUnixTime(year,month,dayOfMonth);



                DateChecker();
            }
        };


        Workout_Analytics_End_Date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int day = cal.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog = new DatePickerDialog(WorkoutAnalytics.this, android.R.style.Theme_Holo_Light_Dialog_MinWidth, DateSetListener_End, year, month, day);

                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        });




        DateSetListener_End = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                chartEndDate=DateSetListener(year,month,dayOfMonth);
                Workout_Analytics_End_Date.setText(chartEndDate);
                dbEndDate = createUnixTime(year,month,dayOfMonth);


                DateChecker();
            }
        };


    }


    public long createUnixTime(int year, int month, int dayOfMonth){
        long epoch = 0;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date dt = sdf.parse(year+ "-" + (month+1)+ "-" +dayOfMonth + "T04:00:00.000-0000");
            epoch = (dt.getTime())/MILLISECONDS_IN_SECOND;


        } catch(ParseException e) {
            e.printStackTrace();
            return epoch;
    }
        return epoch;
    }


    public String DateSetListener(int year, int month, int dayOfMonth){
        String tempMonth;
        String tempDay;

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

        return tempMonth + "/" + tempDay + "/" + year;
    }



    public void DateChecker(){


        if ((!Workout_Analytics_Start_Date.getText().toString().equals("Start Date"))&&(!Workout_Analytics_End_Date.getText().toString().equals("End Date")))
        {



            try {
                workoutsBetweenDateRange =  mWorkoutViewModel.getWorkoutsByDateRange(dbStartDate,dbEndDate);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (workoutsBetweenDateRange.size() > 0) {



                valueCalulations();
                createBarGraph(chartStartDate,chartEndDate);
                barChart.notifyDataSetChanged();
           }
        }
    }


    public void valueCalulations(){
        TextView setTotalDistanceTraveledText = findViewById(R.id.TDT_Data);
        TextView setAvgWorkoutDistanceText = findViewById(R.id.AVG_WD_Data);
        TextView setTimeDurationText = findViewById(R.id.DUR_Data);
        TextView setAvgWorkoutTimeText = findViewById(R.id.AVGWT_Data);

        int TotalDistance = 0;
        int AVGWORKDIST;
        long difference=0;



        for (Workout wrkout : workoutsBetweenDateRange) {
            if((unit_abbrev.compareTo(" m") !=0 )&&(wrkout.getPoolUnits().compareTo("Meters") == 0)){
                TotalDistance += (wrkout.getTotalDistanceTraveled()*METERTOYARDCONVERT);
            }
            else if((unit_abbrev.compareTo(" yd") !=0 )&&(wrkout.getPoolUnits().compareTo("Yards") == 0)){
                TotalDistance += (wrkout.getTotalDistanceTraveled()*YARDTOMETERCONVERT);
            }
            else {
                TotalDistance += wrkout.getTotalDistanceTraveled();
            }
            difference += wrkout.getEndDate().getTime() - wrkout.getStartDate().getTime();
        }


        Date d = new Date(difference);
        DateFormat df = new SimpleDateFormat("HH:mm:ss");
        df.setTimeZone(TimeZone.getTimeZone("UTC"));


        //
        //TOTAL DISTANCE TRAVELED
        //

        String distance = Integer.toString(TotalDistance) + unit_abbrev;
        setTotalDistanceTraveledText.setText(distance);


        //
        //AVERAGE WORKOUT DISTANCE
        //

        AVGWORKDIST = TotalDistance;
        String averageDistance = Integer.toString(AVGWORKDIST/workoutsBetweenDateRange.size() )+ unit_abbrev;

        setAvgWorkoutDistanceText.setText(averageDistance);

        //
        //DURATION
        //

        String time_formatted = df.format(d);
        setTimeDurationText.setText(time_formatted);

        //
        //AVERAGE WORKOUT TIME
        //

        float Avg_Workout_Time= ((float)difference / Float.valueOf(1))/MILLISECONDS_IN_SECOND;
        String avg_time_str = String.format("%.2f sec",(Avg_Workout_Time/workoutsBetweenDateRange.size()));
        setAvgWorkoutTimeText.setText(avg_time_str);

    }






    public void createBarGraph(String Date1, String Date2){
        int count;
        int totaldistance;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

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


            String tempStartDate;
            barEntries = new ArrayList<>();



            for(int j = 0; j< dates.size();j++){
                count = 0;
                totaldistance = 0;
//                Log.d(TAG, "Test" + dates.get(j));

                for(int i = 0; i<=workoutsBetweenDateRange.size()-1;i++) {

                    tempStartDate = simpleDateFormat.format(workoutsBetweenDateRange.get(i).getStartDate());


                    if (dates.get(j).equals(tempStartDate)) {

                        if((unit_abbrev.compareTo(" m") !=0 )&&(workoutsBetweenDateRange.get(i).getPoolUnits().compareTo("Meters") == 0)){
                            totaldistance += (workoutsBetweenDateRange.get(i).getTotalDistanceTraveled()*METERTOYARDCONVERT);
                        }
                        else if((unit_abbrev.compareTo(" yd") !=0 )&&(workoutsBetweenDateRange.get(i).getPoolUnits().compareTo("Yards") == 0)){
                            totaldistance += (workoutsBetweenDateRange.get(i).getTotalDistanceTraveled()*YARDTOMETERCONVERT);
                        }
                        else {
                            totaldistance += workoutsBetweenDateRange.get(i).getTotalDistanceTraveled();
                        }

                        count++;
                    }
                }
                if (count == 0) {
                    count = 1;
                }

                totaldistance = totaldistance / count;
                barEntries.add(new BarEntry(totaldistance,j));
            }

        }catch(ParseException e){
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        BarDataSet barDataSet = new BarDataSet(barEntries,"Distance Swam");
        BarData barData = new BarData(dates,barDataSet);
        barChart.setData(barData);
        barChart.setDescription("");
        barChart.notifyDataSetChanged();

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