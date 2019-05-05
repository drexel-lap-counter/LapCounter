package edu.drexel.lapcounter.lapcounter.backend.wrappers;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import edu.drexel.lapcounter.lapcounter.backend.ble.IBroadcastManager;

public class LocalBroadcastManagerWrapper implements IBroadcastManager {
    private final LocalBroadcastManager mLocalBroadcastManager;

    public LocalBroadcastManagerWrapper(LocalBroadcastManager m) {
        mLocalBroadcastManager = m;
    }

    @Override
    public void sendBroadcast(Intent intent) {
        mLocalBroadcastManager.sendBroadcast(intent);
    }
}
