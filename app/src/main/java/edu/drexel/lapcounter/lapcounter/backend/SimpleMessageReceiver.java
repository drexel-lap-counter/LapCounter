package edu.drexel.lapcounter.lapcounter.backend;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;

import java.util.HashMap;
import java.util.Map;

/**
 * Instead of many boilerplate BroadcastReceivers in the backend, just have ONE owned by the
 * service that keeps a table of callbacks
 */
public class SimpleMessageReceiver extends BroadcastReceiver {
    /**
     * Message handler callback. These will be stored by the receiver and called
     * as needed.
     */
    public interface MessageHandler {
        /**
         * Do something when we get a message.
         * @param message the message
         */
        void onMessage(Intent message);
    }

    /**
     * Map of intent action -> callback function
     */
    private Map<String, MessageHandler> mHandlerTable = new HashMap<>();

    /**
     * The intent filter needed to register the receiver
     */
    private IntentFilter mIntentFilter = new IntentFilter();

    /**
     * Store a callback for handling an intent with the given custom action. The action
     * should be defined as a public static final String in the class of the sender
     * @param handler The callback
     */
    public void registerHandler(String action,  MessageHandler handler) {
        // Record the action in the intent filter
        mIntentFilter.addAction(action);

        // Store the callback
        mHandlerTable.put(action, handler);
    }

    /**
     * Attach this BroadcastReceiver to a context (Activity or Service)
     * @param context the Activity/Service that owns the receiver
     */
    public void attach(Context context) {
//        context.registerReceiver(this, this.mIntentFilter);
        LocalBroadcastManager.getInstance(context).registerReceiver(this, this.mIntentFilter);
    }

    /**
     * If the owner of this receiver needs to stop receiving events, call this method.
     * @param context the parent Activity/Service that owns this receiver
     */
    public void detach(Context context) {
//        context.unregisterReceiver(this);
        LocalBroadcastManager.getInstance(context).unregisterReceiver(this);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        MessageHandler handler = mHandlerTable.get(action);
        if (handler != null) {
            handler.onMessage(intent);
        }
    }
}