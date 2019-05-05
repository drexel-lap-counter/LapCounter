package edu.drexel.lapcounter.lapcounter.backend.wrappers;

import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;

import edu.drexel.lapcounter.lapcounter.backend.ble.IContext;

public class ContextWrapper implements IContext {
    private final Context mContext;

    public ContextWrapper(Context context) {
        mContext = context;
    }

    @Override
    public boolean bindService(Intent service, ServiceConnection connection, int flags) {
        return mContext.bindService(service, connection, flags);
    }

    @Override
    public void unbindService(ServiceConnection connection) {
        mContext.unbindService(connection);
    }

    @Override
    public Context getInner() {
        return mContext;
    }
}
