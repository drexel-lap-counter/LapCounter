package edu.drexel.lapcounter.lapcounter.backend.ble;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

public class BLEService extends Service {
    private final RSSIManager mRssiManager = new RSSIManager(this);

    private final IBinder mBinder = new LocalBinder();

    public class LocalBinder extends Binder {
        public BLEService getService() {
            return BLEService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        // TODO: Bind to BLEComm.
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        // TODO: Unbind from BLEComm.
        super.onDestroy();
    }

    public double getRssi() {
        return mRssiManager.getRssi();
    }

    public int getDirection() {
        return mRssiManager.getDirection();
    }

    public void clearRssiManager() {
        mRssiManager.clear();
    }


}