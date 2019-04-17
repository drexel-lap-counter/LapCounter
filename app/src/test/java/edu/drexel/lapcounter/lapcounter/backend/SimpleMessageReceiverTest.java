package edu.drexel.lapcounter.lapcounter.backend;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SimpleMessageReceiverTest {

    private SimpleMessageReceiver reciever;
    @Mock private SimpleMessageReceiver.MessageHandler handler1;
    @Mock private SimpleMessageReceiver.MessageHandler handler2;
    @Mock Context context;
    @Mock IntentFilter filter;
    @Mock Intent message;

    @Before
    public void setUp() throws Exception {
        reciever = new SimpleMessageReceiver();
    }

    @Test
    public void onReceive_calls_registered_handlers() {
        doNothing().when(filter).addAction("foo");
        reciever.setmIntentFilter(filter);
        reciever.registerHandler("foo", handler1);
        reciever.registerHandler("foo", handler2);
        when(message.getAction()).thenReturn("foo");
        doNothing().when(handler1).onMessage(message);
        doNothing().when(handler2).onMessage(message);

        reciever.onReceive(context, message);

        verify(handler1, times(1)).onMessage(message);
        verify(handler2, times(1)).onMessage(message);
    }

    @Test
    public void onReceive_calls_registered_handlers_selectively() {
        doNothing().when(filter).addAction("foo");
        reciever.setmIntentFilter(filter);
        reciever.registerHandler("foo", handler1);
        reciever.registerHandler("bar", handler2);
        when(message.getAction()).thenReturn("foo");
        doNothing().when(handler1).onMessage(message);
        doNothing().when(handler2).onMessage(message);

        reciever.onReceive(context, message);

        verify(handler1, times(1)).onMessage(message);
        verify(handler2, times(0)).onMessage(message);
    }
}