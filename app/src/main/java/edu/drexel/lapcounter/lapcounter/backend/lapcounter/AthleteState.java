package edu.drexel.lapcounter.lapcounter.backend.lapcounter;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AthleteState that = (AthleteState) o;
        return Double.compare(that.distRssi, distRssi) == 0 &&
                travelDirection == that.travelDirection &&
                timestamp == that.timestamp &&
                zone == that.zone;
    }

    @Override
    public int hashCode() {
        return Objects.hash(zone, distRssi, travelDirection, timestamp);
    }
}
