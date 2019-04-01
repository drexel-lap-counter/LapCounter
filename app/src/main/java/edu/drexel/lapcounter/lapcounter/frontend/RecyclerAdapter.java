package edu.drexel.lapcounter.lapcounter.frontend;


import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import edu.drexel.lapcounter.lapcounter.R;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.MyViewHolder>
{
    private ArrayList<Device> mDataset;

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView mTextView;

        public MyViewHolder(TextView v) {
            super(v);
            mTextView = v;
        }

    }

    public RecyclerAdapter(ArrayList<Device> myDataset)
    {
        mDataset = myDataset;
    }

    public void addItem(Device item)
    {

        //Update this to check for just name and address
        for(int i = 0; i < mDataset.size(); i++)
        {
            Device iDevice = mDataset.get(i);
            if(item.getName().equals(iDevice.getName()) && item.getMac().equals(iDevice.getMac()))
            {
                return;
            }
        }

        //Device not in list, so we add
        mDataset.add(item);


    }

    public Device getDevice(String name)
    {
        for(int i = 0; i < mDataset.size(); i++)
        {
            Device device = mDataset.get(i);
            if(device.getName().equals(name))
            {
                return device;
            }
        }
        return null;
    }
    public Device getDevice(int positon)
    {
        if(positon < 0 || positon >= mDataset.size())
        {
            return null;
        }
        else
        {
            return mDataset.get(positon);
        }
    }



    public void clearItems()
    {
        mDataset.clear();
    }



    @Override
    public RecyclerAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        TextView v = (TextView) LayoutInflater.from(parent.getContext()).inflate(R.layout.device_text_view,parent,false);
        MyViewHolder vh = new MyViewHolder(v);
        return vh;

    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position)
    {
        holder.mTextView.setText(mDataset.get(position).getName());
    }

    @Override
    public int getItemCount()
    {
        return mDataset.size();
    }




}



