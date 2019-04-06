package edu.drexel.lapcounter.lapcounter.backend;

/**
 * All the major parameters for our backend design are stored here for easy modification in
 * one place.
 */
public class Hyperparameters {
    /**
     * This controls the window size for the moving average filter on RSSI values. A larger
     * window means the filtered RSSI values will be less noisy. However, there is a time lag
     * that also increases as the window size grows.
     *
     * @see edu.drexel.lapcounter.lapcounter.backend.ble.RSSIManager
     */
    public static final int RSSI_FILTER_WINDOW_SIZE = 10;
    /**
     * This controls the size of the sliding window of RSSI deltas, used to determine direction.
     * A larger window creates a more accurate value at the cost of a time lag.
     *
     * @see edu.drexel.lapcounter.lapcounter.backend.ble.RSSIManager
     */
    public static final int RSSI_DELTA_WINDOW_SIZE = 3;
    /**
     * The normal polling rate for asking the Bluetooth chip for raw RSSI values.
     *
     * @see edu.drexel.lapcounter.lapcounter.backend.ble.RSSIManager
     */
    public static final int RSSI_POLL_PERIOD_MS = 300;
    /**
     * When trying to reconnect, we want to poll faster, so there is a separate polling rate.
     *
     * @see edu.drexel.lapcounter.lapcounter.backend.ble.RSSIManager
     */
    public static final int RECONNECT_RSSI_POLL_PERIOD_MS = 100;
    /**
     * To count missed laps, it is helpful to determine if the time between disconnect and
     * reconnect was "short" or "long"
     *
     * @see edu.drexel.lapcounter.lapcounter.backend.lapcounter.ReconnectFunction
     */
    public static final int RECONNECT_LONG_DELAY_THRESHOLD_SEC = 30;
}
