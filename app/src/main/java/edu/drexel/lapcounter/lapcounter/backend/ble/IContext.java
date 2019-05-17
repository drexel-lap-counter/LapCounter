package edu.drexel.lapcounter.lapcounter.backend.ble;

import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;

/**
 * Wrapper interface around a Context to help with unit testing. Contexts are hard to mock, so
 * adding a layer in between helps
 */
public interface IContext {
    /**
     * Bind a service
     * @param service a message describing which service to bind
     * @param connection the connection object to a service
     * @param flags anny flags to pass when binding the service
     * @return true if the service binds properly
     */
    boolean bindService(Intent service, ServiceConnection connection, int flags);

    /**
     * Unbind a service
     * @param connection the connection to the service
     */
    void unbindService(ServiceConnection connection);

    /**
     * Get the underlying Context
     * @return the Context
     */
    Context getInner();
}
