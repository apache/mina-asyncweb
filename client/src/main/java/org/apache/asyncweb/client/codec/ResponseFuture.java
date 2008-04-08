/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.apache.asyncweb.client.codec;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeoutException;

import org.apache.asyncweb.client.AsyncHttpClientCallback;

/**
 * Future that wraps the response of an asynchronous HTTP request.  It simply
 * extends FutureTask to borrow AQS.  This future transitions to <tt>done</tt> 
 * through setting the result or the exception.  The instances are considered 
 * one time objects; i.e. once completed, the instance may not be reused.
 * <p>
 * Also, cancellation through this future is not supported.
 */
public class ResponseFuture extends FutureTask<HttpResponseMessage> 
        implements Future<HttpResponseMessage> {
    // dummy instance because I use the FutureTask constructor
    // is not really used...
    private static final Callable<HttpResponseMessage> DUMMY = 
            new Callable<HttpResponseMessage>() {
                public HttpResponseMessage call() throws Exception {
                    return null;
                }
    };
    
    private final HttpRequestMessage request;
    private final BlockingQueue<ResponseFuture> queue;
    private final AsyncHttpClientCallback callback;
    
    /**
     * Constructor.  Optionally one can pass in the completion queue and/or the
     * callback object.
     * 
     * @param queue optional completion queue.  If not null, this future will be
     * placed in the queue on completion.
     * @param callback optional callback object.  If not null, the callback will
     * be invoked at proper stages on completion.
     */
    public ResponseFuture(HttpRequestMessage request, 
            BlockingQueue<ResponseFuture> queue) {
        super(DUMMY);
        this.request = request;
        this.queue = queue;
        this.callback = request.getCallback();
    }
    
    public HttpRequestMessage getRequest() {
        return request;
    }
    
    /**
     * On completion, adds this future object to the completion queue if it is 
     * not null.
     */
    @Override
    protected void done() {
        // add itself to the blocking queue so clients can pick it up
        if (queue != null) {
            queue.add(this);
        }
    }

    /**
     * Sets the response and completes the future.  If a non-null callback was
     * provided, the callback will be invoked 
     * (<tt>AsyncHttpClientCallback.onResponse()</tt>) on the thread on which
     * this method is invoked.
     */
    @Override
    public void set(HttpResponseMessage v) {
        try {
            // fire the callback before completing the future to 
            // ensure everything gets handled before the future gets 
            // completed. 
            if (callback != null) {
                callback.onResponse(v);
            }
        } finally {
            super.set(v);
        }
    }

    /**
     * Sets the exception and completes the future.  If a non-null callback was
     * provided, the callback will be invoked 
     * (<tt>AsyncHttpClientCallback.onException()</tt> or 
     * <tt>AsyncHttpClientCallback.onTimeout()</tt>) on the thread on which
     * this method is invoked.
     */
    @Override
    public void setException(Throwable t) {
        try {
            // fire the callback before completing the future to 
            // ensure everything gets handled before the future gets 
            // completed. 
            if (callback != null) {
                if (t instanceof TimeoutException) {
                    callback.onTimeout();
                } else {
                    callback.onException(t);
                }
            }
        } finally {
            super.setException(t);
        }
    }
    
    /**
     * Canceling via this method is not allowed.  An 
     * UnsupportedOperationException will be thrown.
     */
    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        throw new UnsupportedOperationException("we don't support canceling asynchronous HTTP request via this method...");
    }
}
