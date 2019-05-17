package edu.drexel.lapcounter.lapcounter.backend.Database.Device;

import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import java.util.Objects;

/**
 * BLE Device Object.
 * Class Represents a BLE Device, Is an entity of table devices.
 * Used within the DeviceDAO to interface with the RoomDatabase.
 * Has the following fields:
 * Mac Address (Primary Key)  Mac Address of the Device
 * Name                       Name of the Device
 * Threshold (Non Null)       Calibrated Threshold value of device
 */
@Entity(tableName = "devices")
public class Device
{
    /**
     * Mac Address of the device.  Is a Primary Key in RoomDatabase Table devices
     */
    @ColumnInfo(name="mac_address")
    @PrimaryKey()
    @NonNull
    private String macAddress;

    /**
     * Name of the device.
     */
    @ColumnInfo(name="name")
    private String name;

    /**
     * Calibrated Threshold of device.
     */
    @ColumnInfo(name="threshold")
    @NonNull
    private double threshold;

    /**
     * Empty Constructor that generates Device Object with no data.
     * @Return Empty Device Object
     */
    public Device()
    {
    }

    /**
     * Constructor that generates Device Object with data for all fields.
     * @Param name the name of the device
     * @Param mac_address   the mac address of the device
     * @Param threshold     calibrated device threshold
     */
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

    /**
     * Returns the name of the device
     * @return name of device
     */
    public String getName() {
        return name;
    }
    /**
     * Returns the mac address of the device
     * @return mac address of device
     */
    public String getMacAddress() {
        return macAddress;
    }
    /**
     * Returns the calibrated threshold  of the device
     * @return calibrated threshold of device
     */
    public double getThreshold() {
        return threshold;
    }

    //
    //Setters
    //

    /**
     *  Sets the name of the device to given name.
     * @param name Name of the device
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     *  Sets the mac address of the device to given mac address .
     * @param mac_address mac address of the device
     */
    public void setMacAddress(String mac_address) {
        this.macAddress = mac_address;
    }

    /**
     *  Sets the threshold of the device to given threshold .
     * @param threshold calibrated threshold of the device
     */
    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }

    //
    //Overrides
    //

    /**
     * Overloaded equals operator for comparing equality of Devices.
     * Devices are equal of their names and mac address are the name.
     * @param o Object O to compare equality
     * @return boolean stating whether the two objects are the same
     */
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

    /**
     * Hashs Object to hashCode
     * @return hashcode of object
     */
    @Override
    public int hashCode() {
        return Objects.hash(name, macAddress);
    }
}
