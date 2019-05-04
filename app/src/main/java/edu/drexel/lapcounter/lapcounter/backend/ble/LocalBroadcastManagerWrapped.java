package edu.drexel.lapcounter.lapcounter.backend.ble;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

class LocalBroadcastManagerWrapped implements IBroadcastManager {
    private final LocalBroadcastManager mLocalBroadcastManager;

    public LocalBroadcastManagerWrapped(LocalBroadcastManager m) {
        mLocalBroadcastManager = m;
    }

    @Override
    public void sendBroadcast(Intent intent) {
        mLocalBroadcastManager.sendBroadcast(intent);
    }
}
