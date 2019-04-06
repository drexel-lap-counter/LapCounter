package edu.drexel.lapcounter.lapcounter.frontend;

import android.text.TextUtils;

import java.util.Objects;

public class Device
{


    private String name;
    private String mac;
    private int rssi;

    //Potential want to move Selected to its own seperate class, so
    //we can have multiple classes be selected.
    private boolean Selected;

    public Device(String name,String mac,int rssi)
    {
        this.name = TextUtils.isEmpty(name) ? "Unnamed Device" : name;
        this.mac = mac;
        this.rssi = rssi;
        Selected = false;
    }

    //Getters
    public String getName() {
        return name;
    }

    public String getMac() {
        return mac;
    }

    public int getRssi() {
        return rssi;
    }

    public boolean isSelected() {
        return Selected;
    }

    public void setSelected(boolean selected) {
        Selected = selected;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Device device = (Device) o;
        return Objects.equals(name, device.name) && Objects.equals(mac, device.mac);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, mac);
    }
}
