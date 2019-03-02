package edu.drexel.lapcounter.lapcounter.frontend.PastWorkOutsActivities;

import android.content.Context;
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
    private Context mContext;

    public RecyclerViewAdapter(Context context, ArrayList<String> workoutDate,ArrayList<String> poolLength ) {
        mWorkoutDate = workoutDate;
        mContext = context;
        mPoolLength = poolLength;


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

                Toast.makeText(mContext, mWorkoutDate.get(position), Toast.LENGTH_SHORT).show();

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