package edu.drexel.lapcounter.lapcounter.backend.lapcounter;

import android.os.IBinder;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import edu.drexel.lapcounter.lapcounter.backend.SimpleMessageReceiver;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

// See note in BLEServiceTest.
@RunWith(RobolectricTestRunner.class)
public class LapCounterServiceTest {
    private LocationStateMachine mStateMachine;
    private DisconnectManager mDisconnectManager;
    private LapCounter mLapCounter;

    private SimpleMessageReceiver mReceiver;

    private LapCounterService mService;
    private TransitionLog mLog;

    @Before
    public void setup() {
        mStateMachine = mock(LocationStateMachine.class);
        mDisconnectManager = mock(DisconnectManager.class);
        mLapCounter = mock(LapCounter.class);

        mReceiver = mock(SimpleMessageReceiver.class);
        mLog = mock(TransitionLog.class);

        mService = new LapCounterService(
                mStateMachine, mDisconnectManager, mLapCounter, mReceiver, mLog);
    }

    @Test
    public void bind_service() {
        IBinder binder = mService.onBind(null);
        LapCounterService.LocalBinder lcBinder = (LapCounterService.LocalBinder) binder;
        assertNotNull(lcBinder);
        assertEquals(mService, lcBinder.getService());
    }

    @Test
    public void onDestroy_detaches_receiver() {
        mService.onDestroy();
        verify(mReceiver, times(1)).detach(mService);
    }

    @Test
    public void initCallbacks_forwards_to_components() {
        mService.initCallbacks();

        verify(mStateMachine, times(1)).initCallbacks(mReceiver);
        verify(mDisconnectManager, times(1)).initCallbacks(mReceiver);
        verify(mLapCounter, times(1)).initCallbacks(mReceiver);
    }
}