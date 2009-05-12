/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.ahc.util;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This is an event dispatcher to dispatch monitoring events on separate threads
 * from the main thread.  
 */
public class EventDispatcher {
    /** shared thread pool for dispatching events */
    private static ExecutorService dispatchThreadPool;
    
    /** list of listeners for monitoring events */
    private final List<MonitoringListener> listeners = 
            new CopyOnWriteArrayList<MonitoringListener>();
    
    /**
     * Creates a new EventDispatcher, including starting the new thread. 
     */
    public EventDispatcher() {
        // initialize the thread pool if it is not initialized yet
        synchronized (EventDispatcher.class) {
            if (dispatchThreadPool == null) {
                dispatchThreadPool = Executors.newSingleThreadExecutor(new DaemonThreadFactory());
            }
        }
    }
    
    /**
     * Add a listener for the dispatched events. 
     * 
     * @param listener The new listener.
     */
    public void addListener(MonitoringListener listener) {
        listeners.add(listener);
    }
    
    /**
     * Remove a listener from the dispatch queue.
     * 
     * @param listener The listener to remove.
     */
    public void removeListener(MonitoringListener listener) {
        listeners.remove(listener);
    }
    
    /**
     * Dispatch an event.  
     * 
     * @param event     The event to dispatch.
     */
    public void dispatchEvent(final MonitoringEvent event) { 
        // if there's no active listeners, no point in dispatching this 
        if (listeners.isEmpty()) {
            return;
        }

        Runnable job = new Runnable() {
            public void run() {
                // iterate through the listeners list calling the handlers. 
                for (MonitoringListener listener : listeners) {
                    try {
                        event.dispatch(listener); 
                    } catch (Throwable e) {
                        // just eat these 
                    }
                }
            }
        };
        dispatchThreadPool.execute(job);
    }
}
