package edu.drexel.lapcounter.lapcounter.frontend;


import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import edu.drexel.lapcounter.lapcounter.R;
import edu.drexel.lapcounter.lapcounter.backend.Database.Device.Device;

/**
 * Recycler Adapter for displaying devices in DeviceSelectActivity and DeviceScanActivity with recycler view.
 * This adapter holds the device information, but only displays the name.
 * Zebra striping is done, as well as highlighting of the clicked on device.
 * @see DeviceScanActivity
 * @see DeviceSelectActivity
 */
public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.MyViewHolder> {
    private ArrayList<Device> mDataset;
    private int mSelectedPos = RecyclerView.NO_POSITION;
    private int mZebraStripeColorDark;
    private int mZebraStripeColorLight;

    /**
     * Constructor for Recycler Adapter
     * @param myDataset ArrayList of Devices that will make up the starting data in the recycler.
     * @param light_zebra_color int color value of the light zebra stripe
     * @param dark_zebra_color int color value of the dark zebra stripe
     */
    public RecyclerAdapter(ArrayList<Device> myDataset, int light_zebra_color,int dark_zebra_color) {
        mDataset = myDataset;
        mZebraStripeColorLight = light_zebra_color;
        mZebraStripeColorDark = dark_zebra_color;
    }

    /**
     * Adds a device to the list if it is not in it.
     * @param item Device to be added to list
     */
    public void addItem(Device item) {

        //Update this to check for just name and address
        for (int i = 0; i < mDataset.size(); i++) {
            Device iDevice = mDataset.get(i);
            if (iDevice.equals(item)) {
                return;
            }
        }

        //Device not in list, so we add
        mDataset.add(item);

    }

    /**
     * Adds all devices given to the list if it is not in it.
     * @param devices List of Devices to be added.
     */
    public void addAllItems(ArrayList<Device> devices)
    {
        for(Device device: devices)
        {
            //Update this to check for just name and address
            for (int i = 0; i < mDataset.size(); i++) {
                Device iDevice = mDataset.get(i);
                if (iDevice.equals(device)) {
                    return;
                }
            }

            //Device not in list, so we add
            mDataset.add(device);
        }
    }

    /**
     * Get a device from the dataset based on its name
     * @param name String name of device to look for
     * @return Device object if device with same name found, else null.
     */
    public Device getDevice(String name) {
        for (int i = 0; i < mDataset.size(); i++) {
            Device device = mDataset.get(i);
            if(device.getName().equals(name))
            {
                return device;
            }
        }
        return null;
    }

    /**
     * Get the position of a device with the entered name.
     * @param name String name of the device to look for.
     * @return int position is returned, -1 if not found.
     */
    public int getPosition(String name)
    {
        for(int i = 0; i < mDataset.size(); i++)
        {
            Device device = mDataset.get(i);
            if(device.getName().equals(name))
            {
                return i;
            }
        }
        return -1;
    }

    /**
     * Gets a device from the list based on its position
     * @param positon int position of device
     * @return Device object of device if position in bounds, else null.
     */
    public Device getDevice(int positon) {
        if (positon < 0 || positon >= mDataset.size()) {
            return null;
        } else {
            return mDataset.get(positon);
        }
    }

    /**
     * Set the position of the viewholder that is currently selected
     * @param pos int position of selected item
     */
    public void setSelectedPos(int pos)
    {
        mSelectedPos = pos;
    }

    /**
     * Clear the dataset of all devices
     */
    public void clearItems() {
        mDataset.clear();
    }

    /**
     * Generates a new ViewHolder object for recycler
     * @param parent parent of ViewHolder.
     * @param viewType int value for type of view.
     * @return newly created ViewHolder object
     */
    @Override
    public RecyclerAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        TextView v = (TextView) LayoutInflater.from(parent.getContext()).inflate(R.layout.device_text_view, parent, false);
        MyViewHolder vh = new MyViewHolder(v);
        return vh;

    }

    /**
     * Binds ViewHolders to Recycler View.  Setups up zebra striping and selection highlighting
     * @param holder ViewHolder that is being bound
     * @param position int position of ViewHolder
     */
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        holder.mTextView.setSelected(mSelectedPos == position);
        if (position % 2 == 1)
        {
            holder.mTextView.setBackgroundColor(mZebraStripeColorLight);
        }
        else
        {
            holder.mTextView.setBackgroundColor(mZebraStripeColorDark);
        }

        holder.mTextView.setText(mDataset.get(position).getName());
    }

    /**
     * Gets total item count of dataset
     * @return int size of dataset
     */
    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    /**
     * View holder used for devices in Recycler Adapter.
     */
    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView mTextView;

        public MyViewHolder(TextView v) {
            super(v);
            mTextView = v;
        }

    }


}



