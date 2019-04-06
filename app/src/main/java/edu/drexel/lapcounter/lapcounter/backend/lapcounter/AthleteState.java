package edu.drexel.lapcounter.lapcounter.backend.lapcounter;

/**
 * Snapshot of the estimated state of the athlete.
 */
public class AthleteState{
    /**
     * Near/Far state (no unknown state)
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

    /**
     * Make a copy of the athlete state
     * @return a new Athlete state with the same data
     */
    public AthleteState copy() {
        AthleteState other = new AthleteState();
        other.zone = zone;
        other.distRssi = distRssi;
        other.travelDirection = travelDirection;
        other.timestamp = timestamp;
        return other;
    }
}
