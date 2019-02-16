package edu.drexel.lapcounter.lapcounter.backend;

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
    /**
     * Snapshot of the estimated state of the athlete.
     */
    public class AthleteState {
        /**
         * Near/Far state (no unknown state
         */
        public LocationStateMachine.State zone;
        /**
         * Snapshot filtered RSSI value to sort states by distance (roughly)
         */
        public double distRssi;
        /**
         * Is the athlete out or back?
         * Either RSSIManager.DIRECTION_OUT or DIRECTION_IN
         */
        public int travelDirection;
        /**
         * Unix timestamp of the snapshot
         */
        public long timestamp;
    }

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
     * @return true if an extra lap should be counted, false otherwise.
     */
    boolean computeLapsMissed(AthleteState afterReconnect) {
        mAfterReconnect = afterReconnect;

        // TODO: Complicated Logic goes here. Probably will take a few subroutines.
        return false;
    }

}
