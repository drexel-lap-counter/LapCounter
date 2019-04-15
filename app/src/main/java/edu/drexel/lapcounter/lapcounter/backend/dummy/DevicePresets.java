package edu.drexel.lapcounter.lapcounter.backend.dummy;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * Here is a list of device presets for the Puck, the cheap tags and the Feather.
 * One is listed as the "selected" device, this setting is stored in SharedPreferences of the
 * current workout screen.
 */
public class DevicePresets {
    private static final String PREFS_KEY = "lap_counter_test_devices";
    private static final String KEY_SELECTED_DEVICE ="lap_counter_selected_test_device";

    private static final String[] MAC_ADDRESSES = new String[]{
            "CC:D7:CA:BA:70:E0",
            "E1:27:62:45:75:EF",
            "FF:FF:20:0A:99:8D",
            "D2:AE:39:DB:7B:01",
            "D1:AA:19:79:8A:18"
    };

    private static final String[] NAMES = new String[]{
            "Blue Silicone Tag",
            "Other Tag",
            "Green iTAG",
            "Feather",
            "Puck"
    };

    public static String getDeviceName(Context context) {
        int currentIndex = getSelectedIndex(context);
        return NAMES[currentIndex];
    }

    public static String getAddress(Context context) {
        int currentIndex = getSelectedIndex(context);
        return MAC_ADDRESSES[currentIndex];
    }

    private static int getSelectedIndex(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE);
        return prefs.getInt(KEY_SELECTED_DEVICE, 0);
    }

    private static void setSelectedIndex(Context context, int newIndex) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        final int n = MAC_ADDRESSES.length;
        newIndex = (newIndex % n + n) % n;

        editor.putInt(KEY_SELECTED_DEVICE, newIndex);
        editor.commit();
    }

    public static void incrementSelectedDevice(Context context) {
        setSelectedIndex(context, getSelectedIndex(context) + 1);
    }

    public static void decrementSelectedDevice(Context context) {
        setSelectedIndex(context, getSelectedIndex(context) - 1);
    }
}
