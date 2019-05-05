package edu.drexel.lapcounter.lapcounter.backend.wrappers;

import android.content.Context;
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

    public static LocalBroadcastManagerWrapper getInstance(Context context) {
        return new LocalBroadcastManagerWrapper(LocalBroadcastManager.getInstance(context));
    }
}
