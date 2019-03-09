package edu.drexel.lapcounter.lapcounter.frontend;

import android.content.Context;
import android.content.Intent;
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
public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>{

    private static final String TAG = "RecyclerViewAdapter";

    private ArrayList<String> mWorkoutDate;
    private ArrayList<String> mPoolLength;
    private ArrayList<String> mID;
    private Context mContext;

    public RecyclerViewAdapter(Context context, ArrayList<String> workoutDate,ArrayList<String> poolLength,ArrayList<String> ID ) {
        mWorkoutDate = workoutDate;
        mContext = context;
        mPoolLength = poolLength;
        mID = ID;


    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.past_workout_layout_listitem, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        Log.d(TAG, "onBindViewHolder: called.");


        holder.workoutDate.setText(mWorkoutDate.get(position));
        holder.poolLength.setText(mPoolLength.get(position));


        holder.parentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: clicked on: " + mWorkoutDate.get(position));

                Intent intent = new Intent(mContext,WorkoutDetailsActivity.class);

                intent.putExtra("image_URL",mID.get(position));

                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mWorkoutDate.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView poolLength;
        TextView workoutDate;
        RelativeLayout parentLayout;

        public ViewHolder(View itemView) {
            super(itemView);

            workoutDate = itemView.findViewById(R.id.Date_name);
            poolLength = itemView.findViewById(R.id.txt_pool_sizes);
            parentLayout = itemView.findViewById(R.id.parent_layout);
        }
    }


}