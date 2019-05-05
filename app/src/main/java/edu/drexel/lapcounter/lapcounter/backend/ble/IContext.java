package edu.drexel.lapcounter.lapcounter.backend.ble;

import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;

public interface IContext {
    boolean bindService(Intent service, ServiceConnection connection, int flags);
    void unbindService(ServiceConnection connection);
    Context getInner();
}
