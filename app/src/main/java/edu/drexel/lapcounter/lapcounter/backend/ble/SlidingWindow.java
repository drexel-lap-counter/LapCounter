package edu.drexel.lapcounter.lapcounter.backend.ble;

import java.util.ArrayDeque;

/**
 * A fixed sliding-window of homogeneous elements.
 * @param <T> the type of elements in this sliding-window
 */
public class SlidingWindow<T> extends ArrayDeque<T> {
    /**
     * The size of the sliding window.
     */
    private final int mWindowSize;


    /**
     * Construct a sliding-window with the given fixed size.
     * @param size the size of the sliding window.
     */
    public SlidingWindow(int size) {
        mWindowSize = size;
    }

    /**
     * Add an element to back of the sliding-window.
     *
     * This method will not add an element if {@link #mWindowSize} is 0.
     * This method will remove the first element using {@link ArrayDeque#pollFirst()}
     * if {@link #isFull()}.
     *
     * @param t the element to add
     */
    @Override
    public void addLast(T t) {
        if (mWindowSize == 0) {
            return;
        }

        if (isFull()) {
            pollFirst();
        }

        super.addLast(t);
    }


    /**
     * @return true when the sliding-window has been filled with {@link #mWindowSize} elements,
     * false otherwise.
     */
    public boolean isFull() {
        return size() == mWindowSize;
    }
}
