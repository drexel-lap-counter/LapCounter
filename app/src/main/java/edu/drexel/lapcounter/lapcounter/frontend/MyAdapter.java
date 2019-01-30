package edu.drexel.lapcounter.lapcounter.frontend;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.TextView;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import edu.drexel.lapcounter.lapcounter.R;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder>
{
    private ArrayList<String> mDataset;

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView mTextView;

        public MyViewHolder(TextView v) {
            super(v);
            mTextView = v;
        }

    }

    public MyAdapter(ArrayList<String> myDataset)
    {
        mDataset = myDataset;
    }

    public void AddItem(String item)
    {
        mDataset.add((item));

    }


    @Override
    public MyAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        TextView v = (TextView) LayoutInflater.from(parent.getContext()).inflate(R.layout.my_text_view,parent,false);
        MyViewHolder vh = new MyViewHolder(v);
        return vh;

    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position)
    {
        holder.mTextView.setText(mDataset.get(position));
    }

    @Override
    public int getItemCount()
    {
        return mDataset.size();
    }




}



