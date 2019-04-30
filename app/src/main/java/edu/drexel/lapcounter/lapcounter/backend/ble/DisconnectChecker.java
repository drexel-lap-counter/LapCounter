package edu.drexel.lapcounter.lapcounter.backend.ble;

public class DisconnectChecker {
    private final static int NumConsecutiveRssiBeforeDisconnect = 5;
    private int mConsecutiveRssiCount = 0;
    private Integer mPreviousRssi = null;

    public boolean shouldDisconnect(int rssi) {
        if (mPreviousRssi != null && rssi == mPreviousRssi) {
            ++mConsecutiveRssiCount;

            if (mConsecutiveRssiCount == NumConsecutiveRssiBeforeDisconnect) {
                return true;
            }
        } else {
            mConsecutiveRssiCount = 0;
        }

        mPreviousRssi = rssi;

        return false;
    }

    public void reset() {
        mPreviousRssi = null;
        mConsecutiveRssiCount = 0;
    }
}
