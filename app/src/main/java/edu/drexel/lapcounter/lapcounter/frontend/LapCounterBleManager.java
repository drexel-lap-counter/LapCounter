package edu.drexel.lapcounter.lapcounter.frontend;

import android.bluetooth.BluetoothGatt;
import android.content.Context;

import androidx.annotation.NonNull;
import no.nordicsemi.android.ble.BleManager;
import no.nordicsemi.android.ble.ConnectionPriorityRequest;
import no.nordicsemi.android.ble.ReadRssiRequest;
import no.nordicsemi.android.ble.SleepRequest;

public class LapCounterBleManager extends BleManager<LapCounterBleManagerCallbacks> {

    public LapCounterBleManager(@NonNull Context context) {
        super(context);
    }

    private final BleManagerGattCallback mGattCallback = new BleManagerGattCallback() {
        @Override
        protected boolean isRequiredServiceSupported(@NonNull BluetoothGatt gatt) {
            return true;
        }

        @Override
        protected void onDeviceDisconnected() {}
    };

    @NonNull
    @Override
    protected BleManagerGattCallback getGattCallback() {
        return mGattCallback;
    }

    public ReadRssiRequest readRssi() {
        return super.readRssi();
    }

    public SleepRequest sleep(long delayMs) {
        return super.sleep(delayMs);
    }

    public ConnectionPriorityRequest setConnectionPriority(int priority) {
        return super.requestConnectionPriority(priority);
    }
}
