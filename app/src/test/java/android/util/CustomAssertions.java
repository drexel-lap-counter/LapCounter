package android.util;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class CustomAssertions {
    //https://floating-point-gui.de/errors/comparison/
    public static boolean nearlyEqual(double a, double b, double epsilon) {
        if (a == b) {
            // shortcut, handles infinities
            return true;
        }

        final double diff = Math.abs(a - b);

        if (a == 0 || b == 0 || diff < Double.MIN_NORMAL) {
            // a or b is zero or both are extremely close to it
            // relative error is less meaningful here
            return diff < (epsilon * Double.MIN_NORMAL);
        }

        final double absA = Math.abs(a);
        final double absB = Math.abs(b);

        // use relative error
        return diff / Math.min((absA + absB), Double.MAX_VALUE) < epsilon;
    }

    public static void assertEquals(double a, double b) {
        assertTrue(nearlyEqual(a, b, 10 * Double.MIN_VALUE));
    }

    public static void waitBeforeAssert(long delayMs) {
        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException e) {
            fail("Test interrupted");
        }
    }
}
