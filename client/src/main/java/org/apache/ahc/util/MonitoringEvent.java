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
 
import org.apache.ahc.codec.HttpRequestMessage; 

/**
 * An event triggered during various lifecycle events 
 * of an HTTP request.  Intended for collection of 
 * performance and general monitoring status.
 */
public class MonitoringEvent {
    /**
     * The event was triggered by a new HTTP request.
     */
    public static final int REQUEST_STARTED = 0; 
    /**
     * The event was triggered by successful completion of 
     * an HTTP request.
     */
    public static final int REQUEST_COMPLETED = 1; 
    /**
     * The event was triggered by a failure return from 
     * an HTTP request.
     */
    public static final int REQUEST_FAILED = 2; 
    /**
     * The event was triggered due to a timeout while 
     * waiting for an HTTP request to complete.
     */
    public static final int REQUEST_TIMEOUT = 3; 
    /**
     * Indicates a new connection attempt.
     */
    public static final int CONNECTION_ATTEMPTED = 4; 
    /**
     * Indicates a failure occurred when attempting to 
     * connect to a host.
     */
    public static final int CONNECTION_FAILED = 5; 
    /**
     * Indicates an attempt at retrying a host connect.
     */
    public static final int CONNECTION_RETRIED = 6; 
    /**
     * Indicates an existing connection is being reused
     * for a request.
     */
    public static final int CONNECTION_REUSED = 7; 
    /**
     * Indicates a connection was successfully established 
     * for a request.
     */
    public static final int CONNECTION_SUCCESSFUL = 8; 
    /**
     * The request was redirected to a different location. 
     */
    public static final int REQUEST_REDIRECTED = 9; 
    /**
     * An authentication challenge was received for the request 
     */
    public static final int REQUEST_CHALLENGED = 10; 
    /**
     * Indicates the connection was closed by the server.
     */
    public static final int CONNECTION_CLOSED_BY_SERVER = 11; 
    /**
     * Indicates the connection was closed by the client.
     */
    public static final int CONNECTION_CLOSED = 12; 
    
    /** the type of the event */
    private final int eventType; 
    /** timestamp of when the event occurred */
    private final long timeStamp; 
    /** the request message associated with the event */
    private final HttpRequestMessage request; 
    
    /**
     * Create a new monitoring event.
     * 
     * @param eventType The type of event.
     * @param request   The Http request that his event is triggered by.
     * 
     * @see #REQUEST_STARTED
     * @see #REQUEST_COMPLETED
     * @see #REQUEST_FAILED
     * @see #REQUEST_TIMEOUT
     * @see #CONNECTION_ATTEMPTED
     * @see #CONNECTION_FAILED
     * @see #CONNECTION_RETRIED
     * @see #CONNECTION_REUSED
     * @see #CONNECTION_SUCCESSFUL
     * @see #REQUEST_REDIRECTED
     * @see #REQUEST_CHALLENGED 
     * @see #CONNECTION_CLOSED_BY_SERVER
     * @see #CONNECTION_CLOSED
     */
    public MonitoringEvent(int eventType, HttpRequestMessage request) {
        this.eventType = eventType; 
        this.request = request; 
        // timestamp the event (hi-res)
        this.timeStamp = System.nanoTime()/1000000; 
    }
    
    /**
     * Returns the type code for the event.  
     * 
     * @return The integer type code for the event. 
     */
    public int getType() {
        return eventType; 
    }
    
    /**
     * Get the HTTP request that is associated with this event.
     * 
     * @return The HTTP message request being processed when one 
     *         of these events occurred.
     */
    public HttpRequestMessage getRequest() 
    {
        return request; 
    }
    
    /**
     * Returns the timestamp that was taken when the event 
     * was generated. 
     * 
     * @return The hi-res timer value (in ms) for when the 
     *         event was generated.
     */
    public long getTimeStamp() {
        return timeStamp; 
    }
    
    /**
     * Dispatch an event to a listener. 
     * 
     * @param listener The target listener
     */
    public void dispatch(Object listener) {
        ((MonitoringListener)listener).notification(this);
    }
}
