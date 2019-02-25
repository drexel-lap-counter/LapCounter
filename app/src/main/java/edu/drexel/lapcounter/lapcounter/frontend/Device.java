package edu.drexel.lapcounter.lapcounter.frontend;

/**
 * class for representing bluetooth tags that are being used
 */
public class Device
{


    /**
     * the displayed name of the device
     */
    private String name;
    /**
     * the device's hardware address
     */
    private String mac;
    /**
     * the distance threshold for this device
     */
    private int rssi;

    //Potential want to move Selected to its own seperate class, so
    //we can have multiple classes be selected.
    /**
     * indicates weather or not this device is currently in use
     */
    private boolean Selected;

    /**
     * @param name
     * @param mac
     * @param rssi
     */
    public Device(String name,String mac,int rssi)
    {
        this.name = name;
        this.mac = mac;
        this.rssi = rssi;
        Selected = false;
    }

    /**
     * @return String name
     */
    //Getters
    public String getName() {
        return name;
    }

    /**
     * @return String Mac address
     */
    public String getMac() {
        return mac;
    }

    /**
     * @return int rssi
     */
    public int getRssi() {
        return rssi;
    }

    /**
     * @return true of the device is selected, false otherwise
     */
    public boolean isSelected() {
        return Selected;
    }

    /**
     * @param selected
     */
    public void setSelected(boolean selected) {
        Selected = selected;
    }

    /**
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @param mac
     */
    public void setMac(String mac) {
        this.mac = mac;
    }

    /**
     * @param rssi
     */
    public void setRssi(int rssi) {
        this.rssi = rssi;
    }


}
