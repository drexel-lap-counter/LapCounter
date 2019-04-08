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
    private double threshold;

    public Device()
    {
    }


    public Device(String name, String mac_address, double threshold)
    {
        String defaultName = String.format("??? (%s)", mac_address);

        this.name = TextUtils.isEmpty(name) ? defaultName : name;
        this.macAddress = mac_address;
        this.threshold = threshold;
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

    public double getThreshold() {
        return threshold;
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

    public void setThreshold(double threshold) {
        this.threshold = threshold;
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
