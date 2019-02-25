package edu.drexel.lapcounter.lapcounter.backend.dummy;

import android.os.Handler;

import java.util.List;
import java.util.Random;

import edu.drexel.lapcounter.lapcounter.backend.ble.DeviceScanner;

public class DummyDeviceScanner implements DeviceScanner {

    private Callback mCallback = null;
    private Handler mHandler = new Handler();
    private Random mRand = new Random();
    private List<String> mWhitelist = null;

    private static final String[] DEVICE_NAMES = new String[] {
        "Dummy A",
        "Dummy B",
        "Dummy C",
        "Dummy D",
        "Dummy E",
        "Dummy F",
        "Dummy G",
        "Dummy H",
        "Dummy I",
        "Dummy J"
    };

    private static final String[] MAC_ADDRESSES  = new String[] {
        "FF:FF:FF:FF:FF:00",
        "FF:FF:FF:FF:FF:01",
        "FF:FF:FF:FF:FF:02",
        "FF:FF:FF:FF:FF:03",
        "FF:FF:FF:FF:FF:04",
        "FF:FF:FF:FF:FF:05",
        "FF:FF:FF:FF:FF:06",
        "FF:FF:FF:FF:FF:07",
        "FF:FF:FF:FF:FF:08",
        "FF:FF:FF:FF:FF:09"
    };

    private Runnable detectDummyDevice = new Runnable() {
        @Override
        public void run() {
            // Generate a device name with a random suffix
            byte[] randSuffix = new byte[3];
            mRand.nextBytes(randSuffix);
            StringBuilder suffix = new StringBuilder();
            for (byte x : randSuffix) {
                suffix.append(String.format("%02X", x));
            }
            String name = "Dummy BLE Device " + suffix.toString();

            // Build a random MAC address
            byte[] randAddress = new byte[6];
            mRand.nextBytes(randAddress);
            StringBuilder addressBuilder = new StringBuilder();
            for (byte x : randAddress) {
                addressBuilder.append(":" + String.format("%02X", x));
            }
            // trim off the beginning :
            String address = addressBuilder.toString().substring(1);


            // Random RSSI value from -30 to -80
            int rssi = -(30 + mRand.nextInt(50));


            if (mCallback != null)
                mCallback.onDeviceFound(name, address, rssi);
        }
    };
    ;

    @Override
    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    @Override
    public void setAddressWhitelist(List<String> allowedAddresses) {
        mWhitelist = allowedAddresses;
    }

    @Override
    public void startScan() {
        int delay = 500;
        for (int i = 0; i < DEVICE_NAMES.length; i++) {
            // Callbacks can only use final variables
            final int finalI = i;
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    maybeFindDummyDevice(finalI);
                }
            }, delay);
            // Add random time delays between each device
            delay += mRand.nextInt(100);
        }

    }

    @Override
    public void stopScan() {
        // Nothing to do here for the dummy scanner :)
    }

    /**
     * "detect" a dummy device
     * @param i the index of the dummy device
     */
    private void maybeFindDummyDevice(int i) {
        // If there's no callback, don't do anything
        if (mCallback == null)
            return;

        // Pull out the i-th dummy value
        String name = DEVICE_NAMES[i];
        String address = MAC_ADDRESSES[i];
        // Random RSSII value in the range [-30, -80)
        int rssi = -(30 + mRand.nextInt(50));

        // If we don't have a whitelist or the given address is whitelist,
        // call the callback
        if (mWhitelist == null || isWhitelisted(address))
            mCallback.onDeviceFound(name, address, rssi);
    }

    /**
     * Check if a MAC address is on the whitelist
     * @param address the address
     * @return true if the address is in mWhitelist
     */
    private boolean isWhitelisted(String address) {
        for (String addr : mWhitelist) {
            if (addr.equals(address))
                return true;
        }
        return false;
    }
}
