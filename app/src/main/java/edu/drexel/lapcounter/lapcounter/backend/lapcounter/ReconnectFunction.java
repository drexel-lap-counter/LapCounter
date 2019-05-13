package edu.drexel.lapcounter.lapcounter.backend.lapcounter;

import edu.drexel.lapcounter.lapcounter.backend.Hyperparameters;
import edu.drexel.lapcounter.lapcounter.backend.ble.RSSIManager;

/**
 * This class handles the somewhat complicated logic for determining if a lap was missed due
 * to a disconnection. It represents a function
 *
 * reconnect_func :: (AthleteState, AthleteState) -> bool
 *
 * Given the before and after states of the athlete (see below), this function will return true
 * if a lap was missed.
 */
public class ReconnectFunction {
    private static final int LONG_DELAY_THRESHOLD_SEC =
            Hyperparameters.RECONNECT_LONG_DELAY_THRESHOLD_SEC;

    /**
     * State just before disconnecting
     */
    private AthleteState mBeforeDisconnect;
    /**
     * State just after reconnecting
     */
    private AthleteState mAfterReconnect;

    /**
     * Store the state just before the disconnect for later diagnostics.
     * @param beforeDisconnect the state just before the D/C
     */
    void setBeforeState(AthleteState beforeDisconnect) {
        mBeforeDisconnect = beforeDisconnect;
    }

    /**
     * After reconnecting and getting the current state, examine the state carefully and
     * determine if we missed a lap.
     * @param afterReconnect first valid state after a reconnect
     * @return true if a missed lap should be counted, false otherwise.
     */
    boolean computeLapsMissed(AthleteState afterReconnect) {
        mAfterReconnect = afterReconnect;

        // States are either near or far at this point in the code
        boolean wasFar = mBeforeDisconnect.zone == LocationStateMachine.State.FAR;
        boolean isFar = mAfterReconnect.zone == LocationStateMachine.State.FAR;

        long delay = (mAfterReconnect.timestamp - mBeforeDisconnect.timestamp) / 1000;
        boolean isLongDelay = delay >= LONG_DELAY_THRESHOLD_SEC;

        // Far -> Near is always one lap
        if (wasFar && !isFar)
            return true;

        // If we started and ended in the near zone after a long pause, most likely the swimmer
        // left the near zone and came back
        if (!wasFar && !isFar && isLongDelay)
            return true;

        // In all other cases, we do not count a lap
        return false;
    }

}
