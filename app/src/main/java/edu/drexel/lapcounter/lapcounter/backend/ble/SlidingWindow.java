package edu.drexel.lapcounter.lapcounter.backend.ble;

import java.util.ArrayDeque;

public class SlidingWindow<T> extends ArrayDeque<T> {
    private final int mWindowSize;

    public SlidingWindow(int size) {
        mWindowSize = size;
    }

    @Override
    public void addLast(T t) {
        if (isFull()) {
            pollFirst();
        }

        super.addLast(t);
    }

    public boolean isFull() {
        return size() == mWindowSize;
    }
}
