package edu.drexel.lapcounter.lapcounter.backend.dummy;

import android.os.Handler;

import java.util.List;
import java.util.Random;

import edu.drexel.lapcounter.lapcounter.backend.BLEScanner;

public class DummyDeviceScanner implements BLEScanner {
    private static final int NUM_DUMMY_DEVICES = 10;


    private Callback mCallback = null;
    private Handler mHandler = new Handler();
    private Random mRand = new Random();

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

    @Override
    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    @Override
    public void setAddressWhitelist(List<String> allowedAddresses) {
        // The dummy data is random anyway, so whitelisting is a no-op
    }

    @Override
    public void startScan() {
        int delay = 500;
        for (int i = 0; i < NUM_DUMMY_DEVICES; i++) {
            mHandler.postDelayed(detectDummyDevice, delay);
            // Add random time delays between each device
            delay += mRand.nextInt(100);
        }

    }

    @Override
    public void stopScan() {
        // Nothing to do here for the dummy scanner :)
    }
}
