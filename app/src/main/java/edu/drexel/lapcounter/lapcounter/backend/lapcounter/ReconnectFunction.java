package edu.drexel.lapcounter.lapcounter.backend.lapcounter;

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
    private static final int LONG_DELAY_THRESHOLD_SEC = 30;

    // States immediately before and after a disconnection.
    private AthleteState mBeforeDisconnect;
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

        // Since the far zone is very large, after a long delay it's hard to tell whether
        // we need to count a lap. use other information to get a better picture
        if (wasFar && isFar && isLongDelay)
            return doubleCheckFarFar();

        // In all other cases, we do not count a lap
        return false;
    }

    /**
     * For the case of before.zone = Far, after.zone = Far, delay = Long, it's hard to tell
     * what happened. Use other information from the RSSIManager to reconstruct what happened
     *
     * @return true if a lap should be counted
     */
    boolean doubleCheckFarFar() {
        // Is the athlete moving inbound or outbound before and after the disconnection?
        boolean wasInbound = mBeforeDisconnect.travelDirection == RSSIManager.DIRECTION_IN;
        boolean isInbound = mAfterReconnect.travelDirection == RSSIManager.DIRECTION_IN;

        // If we were inbound and now we're outbound, the path must have dipped inside the
        // near zone.
        if (wasInbound && !isInbound)
            return true;

        // Sort the distance estimates before and after disconnecting. Is the after state
        // further away or closer than the old one? This helps distinguish the last two cases
        boolean isFurtherAway = mBeforeDisconnect.distRssi < mAfterReconnect.distRssi;

        // If both states are inbound but now the athlete is further away, the athlete's path must
        // have gone through the near zone and and out before eventually turning around
        if (wasInbound && isInbound && isFurtherAway)
            return true;

        // Similar to the previous case, if both states are outbound but the athlete is now
        // closer to the phone, the path must have gone through the near zone.
        if (!wasInbound && !isInbound && !isFurtherAway)
            return true;

        // In all other cases, do not count a lap
        return false;
    }
}
