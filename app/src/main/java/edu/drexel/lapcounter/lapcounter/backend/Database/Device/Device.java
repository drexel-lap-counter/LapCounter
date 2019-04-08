package edu.drexel.lapcounter.lapcounter.backend.Database.Device;

import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import java.util.Objects;

@Entity(tableName = "devices")
public class Device
{
    @ColumnInfo(name="mac_address")
    @PrimaryKey()
    @NonNull
    private String macAddress;

    @ColumnInfo(name="name")
    private String name;

    @ColumnInfo(name="threshold")
    @NonNull
    private int threshold;



    //Potential want to move Selected to its own seperate class, so
    //we can have multiple classes be selected.
    private boolean Selected;

    public Device()
    {
    }


    public Device(String name, String mac_address, int threshold)
    {
        this.name = TextUtils.isEmpty(name) ? "Unnamed Device" : name;
        this.macAddress = mac_address;
        this.threshold = threshold;
        Selected = false;
    }


    //
    //Getters
    //
    public String getName() {
        return name;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public int getThreshold() {
        return threshold;
    }

    public boolean isSelected() {
        return Selected;
    }


    //
    //Setters
    //
    public void setName(String name) {
        this.name = name;
    }

    public void setMacAddress(String mac_address) {
        this.macAddress = mac_address;
    }

    public void setThreshold(int threshold) {
        this.threshold = threshold;
    }

    public void setSelected(boolean selected) {
        Selected = selected;
    }


    //
    //Overrides
    //
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Device device = (Device) o;
        return Objects.equals(name, device.name) && Objects.equals(macAddress, device.macAddress);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, macAddress);
    }
}
