package edu.drexel.lapcounter.lapcounter.backend.ble;

import android.util.CustomAssertions;

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
    public void filter_returns_element_when_buffer_has_one_element() {
        MovingAverage ma = new MovingAverage(10);
        double value = 84;
        CustomAssertions.assertEquals(ma.filter(value), value);
    }

    @Test
    public void filter_returns_midpoint_when_buffer_has_two_elements() {
        double value1 = 50;
        double value2 = 100;

        MovingAverage ma = new MovingAverage(10);
        ma.filter(value1);

        CustomAssertions.assertEquals(ma.filter(value2), (value1 + value2) / 2);
    }

    @Test
    public void filter_returns_average_up_to_and_including_full_buffer() {
        final int size = 100;
        MovingAverage ma = new MovingAverage(size);

        for (int i = 1; i <= size; ++i) {
            CustomAssertions.assertEquals(ma.filter(i), (i + 1) / 2.0);
        }
    }

    @Test
    public void filter_returns_average_after_adding_one_element_to_full_buffer() {
        final int size = 100;
        MovingAverage ma = new MovingAverage(size);

        for (int i = 1; i <= size; ++i) {
            ma.filter(i);
        }

        CustomAssertions.assertEquals(ma.filter(size + 1), 1 + (size + 1) / 2.0);
    }

    @Test
    public void filter_returns_average_after_adding_k_elements_to_full_buffer() {
        final int size = 100;
        final int k = 50;

        MovingAverage ma = new MovingAverage(size);

        for (int i = 1; i <= size; ++i) {
            ma.filter(i);
        }

        for (int i = 1; i <= k; ++i) {
            CustomAssertions.assertEquals(ma.filter(size + i), i + (size + 1) / 2.0);
        }
    }


    @Test
    public void clear() {
    }
}