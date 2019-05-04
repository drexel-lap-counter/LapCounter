package edu.drexel.lapcounter.lapcounter.backend.ble;

import android.content.Intent;

interface IBroadcastManager {
    void sendBroadcast(Intent intent);
}
