package edu.drexel.lapcounter.lapcounter.frontend;

import android.content.Context;
import android.content.Intent;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import java.util.ArrayList;
import edu.drexel.lapcounter.lapcounter.R;

/**
 * Class is an adapter used with a recycler view that handles the data storage and displaying.
 * Used to bind views to the recycler view to display
 */
public class PastWorkoutsRecyclerAdapter extends RecyclerView.Adapter<PastWorkoutsRecyclerAdapter.ViewHolder>{

    private static final String TAG = "RecyclerViewAdapter";

    private ArrayList<String> mWorkoutDate;
    private ArrayList<String> mPoolLength;

    private ArrayList<String> mLaps;
    private ArrayList<String> mID;
    private Context mContext;

    /**
     * Constructor for creating adapter
     * @param context Context of Activity that is creating this.
     * @param workoutDate ArrayList of workout dates (Start Date)
     * @param laps ArrayList of laps
     * @param ID Arraylist of workout ids
     */
    public PastWorkoutsRecyclerAdapter(Context context, ArrayList<String> workoutDate,ArrayList<String> laps,ArrayList<String> ID ) {
        mWorkoutDate = workoutDate;
        mContext = context;
        mLaps = laps;
        mID = ID;


    }

    /**
     * Creates ViewHolder to hold workout data
     * @param parent ViewGroup parent
     * @param viewType int that specifies the viewType.
     * @return The created ViewHolder object
     */
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_past_workouts_layout_listitem, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    /**
     * Sets the view holder values for a specific workout
     * @param holder ViewHolder for displaying workout data
     * @param position position the ViewHolder is in the list
     */
    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        Log.d(TAG, "onBindViewHolder: called.");


        holder.workoutDate.setText(mWorkoutDate.get(position));
        holder.laps.setText(mLaps.get(position));




        holder.parentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: clicked on: " + mWorkoutDate.get(position));
                Intent intent = new Intent(mContext, WorkoutDetailsActivity.class);
                int id = Integer.parseInt(mID.get(position));

                intent.putExtra(WorkoutDetailsActivity.EXTRAS_WORKOUT_ID, id);
                mContext.startActivity(intent);

            }
        });
    }

    /**
     * Gets the total amount of items in the list
     * @return int number of items in list
     */
    @Override
    public int getItemCount() {
        return mWorkoutDate.size();
    }

    /**
     * ViewHolder class for workout data.
     * Contains the workout date, as well as number of laps completed for that workout
     */
    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView workoutDate;
        TextView laps;
        ConstraintLayout parentLayout;

        public ViewHolder(View itemView) {
            super(itemView);

            workoutDate = itemView.findViewById(R.id.Date_name);
            laps = itemView.findViewById(R.id.txt_pool_sizes);
            parentLayout = itemView.findViewById(R.id.parent_layout);

        }
    }


}