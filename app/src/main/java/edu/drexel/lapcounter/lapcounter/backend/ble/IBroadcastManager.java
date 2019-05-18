package edu.drexel.lapcounter.lapcounter.backend.ble;

import android.content.Intent;

/**
 * This is a wrapper interface around a BroadcastManager. This is used  for unit testing
 * since BroadcastManager is difficult to mock.
 */
public interface IBroadcastManager {
    /**
     * Broadcast an intent to other components
     * @param intent the message to send.
     */
    void sendBroadcast(Intent intent);
}
