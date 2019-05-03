package edu.drexel.lapcounter.lapcounter.backend.ble;

import org.junit.Test;

import static org.junit.Assert.*;

public class MovingAverageTest {

    @Test
    public void windowIsFull_true_when_full() {
        final int size = 10;
        MovingAverage ma = new MovingAverage(size);
        for (int i = 0; i < size; ++i) {
            ma.filter(i);
        }

        assertTrue(ma.windowIsFull());
    }

    @Test
    public void windowIsFull_false_when_empty() {
        assertFalse(new MovingAverage(1).windowIsFull());
    }

    @Test
    public void windowIsFull_false_when_partially_filled() {
        final int size = 10;
        MovingAverage ma = new MovingAverage(size);
        for (int i = 0; i < size/2; ++i) {
            ma.filter(i);
        }

        assertFalse(ma.windowIsFull());
    }


    @Test
    public void filter() {

    }

    @Test
    public void clear() {
    }
}