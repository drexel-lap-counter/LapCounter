package edu.drexel.lapcounter.lapcounter.frontend;

import android.content.Context;
import android.text.Editable;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import edu.drexel.lapcounter.lapcounter.R;

import static org.junit.Assert.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PoolSizeActivityTest {

    PoolSizeActivity poolSizeActivity;
    @Mock
    RadioButton radioButton;
    @Mock
    EditText custom_pool_text;

    @Before
    public void setup(){
        poolSizeActivity = new PoolSizeActivity();
    }

    @Test
    public void onPoolSizeRadioButtonClicked_sets_correct_size_25() {
        when(radioButton.getId()).thenReturn(R.id.pool_size_25);
        when(radioButton.isChecked()).thenReturn(true);
        when(custom_pool_text.isEnabled()).thenReturn(false);
        poolSizeActivity.setCustom_pool_text(custom_pool_text);
        //radioButton.setId(R.id.pool_size_25);

        poolSizeActivity.onPoolSizeRadioButtonClicked(radioButton);

        assertEquals(25, poolSizeActivity.getPoolSize());
    }

    @Test
    public void onPoolSizeRadioButtonClicked_sets_correct_size_50() {
        when(radioButton.getId()).thenReturn(R.id.pool_size_50);
        when(radioButton.isChecked()).thenReturn(true);
        when(custom_pool_text.isEnabled()).thenReturn(false);
        poolSizeActivity.setCustom_pool_text(custom_pool_text);

        poolSizeActivity.onPoolSizeRadioButtonClicked(radioButton);

        assertEquals(50, poolSizeActivity.getPoolSize());
    }

    @Test
    public void onPoolUnitsRadioButtonClicked_sets_correct_size_custom() {
        when(radioButton.getId()).thenReturn(R.id.pool_size_custom);
        when(radioButton.isChecked()).thenReturn(true);
        when(custom_pool_text.isEnabled()).thenReturn(false);
        Editable custom_length = Mockito.mock(Editable.class);
        when(custom_length.toString()).thenReturn("33");
        when(custom_pool_text.getText()).thenReturn(custom_length);
        poolSizeActivity.setCustom_pool_text(custom_pool_text);

        poolSizeActivity.onPoolSizeRadioButtonClicked(radioButton);

        assertEquals(33, poolSizeActivity.getPoolSize());
    }

    @Test
    public void onPoolUnitsRadioButtonClicked_disables_custom_field_25() {
        when(radioButton.getId()).thenReturn(R.id.pool_size_25);
        when(radioButton.isChecked()).thenReturn(true);
        when(custom_pool_text.isEnabled()).thenReturn(true);
        poolSizeActivity.setCustom_pool_text(custom_pool_text);

        poolSizeActivity.onPoolSizeRadioButtonClicked(radioButton);

        verify(custom_pool_text, times(1)).setEnabled(false);
    }

    @Test
    public void onPoolUnitsRadioButtonClicked_disables_custom_field_50() {
        when(radioButton.getId()).thenReturn(R.id.pool_size_50);
        when(radioButton.isChecked()).thenReturn(true);
        when(custom_pool_text.isEnabled()).thenReturn(true);
        poolSizeActivity.setCustom_pool_text(custom_pool_text);

        poolSizeActivity.onPoolSizeRadioButtonClicked(radioButton);

        verify(custom_pool_text, times(1)).setEnabled(false);
    }

    @Test
    public void onPoolUnitsRadioButtonClicked_enables_custom_field_custom() {
        when(radioButton.getId()).thenReturn(R.id.pool_size_custom);
        when(radioButton.isChecked()).thenReturn(true);
        when(custom_pool_text.isEnabled()).thenReturn(false);
        poolSizeActivity.setCustom_pool_text(custom_pool_text);
        Editable custom_length = Mockito.mock(Editable.class);
        when(custom_length.toString()).thenReturn("33");
        when(custom_pool_text.getText()).thenReturn(custom_length);
        poolSizeActivity.setCustom_pool_text(custom_pool_text);

        poolSizeActivity.onPoolSizeRadioButtonClicked(radioButton);

        verify(custom_pool_text, times(1)).setEnabled(true);
    }
}