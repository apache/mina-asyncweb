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
package org.apache.ahc.codec;

import java.net.URL;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.ahc.AsyncHttpClient;
import org.apache.ahc.AsyncHttpClientCallback;
import org.apache.ahc.auth.AuthChallengeParser;
import org.apache.ahc.auth.AuthPolicy;
import org.apache.ahc.auth.AuthScheme;
import org.apache.ahc.auth.AuthState;
import org.apache.ahc.util.MonitoringEvent;
import org.apache.ahc.util.NameValuePair;
import org.apache.mina.common.IoHandlerAdapter;
import org.apache.mina.common.IoSession;


/**
 * The Class HttpIoHandler.  Implements the MINA IoHandler interface as the primary
 * event processor for the HTTP communication.
 */
public class HttpIoHandler extends IoHandlerAdapter {

    /**
     * The Constant CURRENT_REQUEST.
     */
    public static final String CURRENT_REQUEST = "CURRENT_REQUEST";

    /**
     * The Constant CURRENT_RESPONSE.
     */
    public static final String CURRENT_RESPONSE = "CURRENT_RESPONSE";
    
    /**
     * The Constant CONNECTION_CLOSE.
     */
    public static final String CONNECTION_CLOSE = "close";

    /**
     * The scheduler service to handle timeouts.
     */
    private final ScheduledExecutorService scheduler;

    /**
     * The session cache used for reusable connections 
     */
    private SessionCache sessionCache; 

    /**
     * Instantiates a new HttpIoHandler with a new a single-threaded executor.
     */
    public HttpIoHandler() {
        this(Executors.newSingleThreadScheduledExecutor());
    }

    /**
     * Instantiates a new HttpIoHandler with the supplied scheduler.
     *
     * @param scheduler the scheduler to use to track timeouts
     */
    public HttpIoHandler(ScheduledExecutorService scheduler) {
        this.scheduler = scheduler;
    }

    /**
     * Set the session cache that should be used for 
     * connection reuse.
     * 
     * @param cache  The new session cache.  If null, this will disable
     *               future connection reuse.
     */
    public void setSessionCache(SessionCache cache) {
        sessionCache = cache; 
    }
    
    /**
     * Retrieve the session cache used for storing 
     * connections for reuse. 
     * 
     * @return The current session cache for the client. 
     */
    public SessionCache getSessionCache() {
        return sessionCache; 
    }

    /**
     * Destroys the handler and shuts down the scheduler.
     */
    public void destroy() {
        scheduler.shutdownNow();
    }

    /**
     * Stub for handling sessionOpened events.
     *
     * @see org.apache.mina.common.IoHandlerAdapter#sessionOpened(org.apache.mina.common.IoSession)
     */
    public void sessionOpened(IoSession ioSession) throws Exception {
    }

    /**
     * Handler for receiving a response from a remote server.
     *
     * @param ioSession the {@link org.apache.mina.common.IoSession} representing the connection to the server.
     * @param object    the {@link HttpResponseMessage} object
     * @see org.apache.mina.common.IoHandlerAdapter#messageReceived(org.apache.mina.common.IoSession,java.lang.Object)
     */
    public void messageReceived(IoSession ioSession, Object object) throws Exception {

        HttpResponseMessage response = (HttpResponseMessage) object;

        HttpRequestMessage request = (HttpRequestMessage) ioSession.getAttribute(CURRENT_REQUEST);

        AsyncHttpClient client = (AsyncHttpClient) ioSession.getAttachment();

        //Check if we are to handle redirects
        if ((response.getStatusCode() == 301
             || response.getStatusCode() == 302
             || response.getStatusCode() == 307)
            && request.isFollowRedirects())
        {

            // notify any interesting parties that this is starting 
            client.notifyMonitoringListeners(MonitoringEvent.REQUEST_REDIRECTED, request); 
            //Change the request url to the redirect
            request.setUrl(new URL(response.getLocation()));
            // if we're redirected via 30x, the request method should be reset to GET
            if (!request.getRequestMethod().equals(HttpRequestMessage.REQUEST_GET)) {
                request.setRequestMethod(HttpRequestMessage.REQUEST_GET);
            }
            // we also need to clear out the parameters
            request.clearAllParameters();

            //Send the redirect
            client.sendRequest(request);

            // if we've been provided with a cache, put this session into 
            // the cache. 
            SessionCache cache = getSessionCache(); 
            if (cache != null) {
            // cache the session before we return
                cache.cacheSession(ioSession);
            }
            return;
        }

        //Check if we have authentication
        if (response.getChallenges().size() > 0) {
            // notify any interesting parties that this is starting 
            client.notifyMonitoringListeners(MonitoringEvent.REQUEST_CHALLENGED, request); 
            for (NameValuePair nvp : response.getChallenges()) {
                AuthState state = request.getAuthState();
                if (state == null) {
                    String id = AuthChallengeParser.extractScheme(nvp.getValue());
                    AuthScheme authScheme = AuthPolicy.getAuthScheme(id);
                    state = new AuthState();
                    state.setAuthScheme(authScheme);
                    authScheme.processChallenge(nvp.getValue());
                    request.setAuthState(state);
                }
            }

            //Authenticate
            int authCount = request.getAuthCount() + 1;
            if (authCount <= 3) {
                // if we've been provided with a cache, put this session into 
                // the cache. 
                SessionCache cache = getSessionCache(); 
                if (cache != null) {
                    // cache the session before we return
                    cache.cacheSession(ioSession);
                }
                
                request.setAuthCount(authCount);
                client.sendRequest(request);

                return;
            }
        }

        cancelTasks(request);

        // notify any interesting parties that this is starting 
        client.notifyMonitoringListeners(MonitoringEvent.REQUEST_COMPLETED, request); 
        // complete the future which will also fire the callback
        ResponseFuture result = request.getResponseFuture();
        result.set(response);

        // if we've been provided with a cache, put this session into 
        // the cache. 
        SessionCache cache = getSessionCache(); 
        if (cache != null) {
        // cache the session before we return
            cache.cacheSession(ioSession);
        }
    }

    /**
     * Handler for receiving a notification that an Exception occurred in the communication with the server
     *
     * @param ioSession the {@link org.apache.mina.common.IoSession} representing the connection to the server.
     * @param throwable the {@link java.lang.Throwable} object representing the exception that occurred
     * @see org.apache.mina.common.IoHandlerAdapter#exceptionCaught(org.apache.mina.common.IoSession,java.lang.Throwable)
     */
    public void exceptionCaught(IoSession ioSession, Throwable throwable) throws Exception {
        //Clean up if any in-proccess decoding was occurring
        ioSession.removeAttribute(CURRENT_RESPONSE);

        HttpRequestMessage request = (HttpRequestMessage) ioSession.getAttribute(CURRENT_REQUEST);
        cancelTasks(request);

        AsyncHttpClient client = (AsyncHttpClient) ioSession.getAttachment();

        // notify any interesting parties that this is starting 
        client.notifyMonitoringListeners(MonitoringEvent.REQUEST_FAILED, request); 
        // complete the future which will also fire the callback
        ResponseFuture result = request.getResponseFuture();
        result.setException(throwable);

        //Exception is bad, so just close it up
        ioSession.close();
    }

    /**
     * Handler for notifying that a connection was closed to the remote server.
     *
     * @param ioSession the {@link org.apache.mina.common.IoSession} representing the connection to the server.
     * @see org.apache.mina.common.IoHandlerAdapter#sessionClosed(org.apache.mina.common.IoSession)
     */
    public void sessionClosed(IoSession ioSession) throws Exception {
        //Clean up if any in-proccess decoding was occurring
        ioSession.removeAttribute(CURRENT_RESPONSE);
        
        // if we've been provided with a cache, remove this session from 
        // the cache. 
        SessionCache cache = getSessionCache(); 
        if (cache != null) {
            // cache the session before we return
            cache.removeSession(ioSession);
        }
        HttpRequestMessage request = (HttpRequestMessage) ioSession.getAttribute(CURRENT_REQUEST);
        cancelTasks(request);
        AsyncHttpClient client = (AsyncHttpClient) ioSession.getAttachment();
        // notify any interesting parties that this is starting 
        client.notifyMonitoringListeners(MonitoringEvent.CONNECTION_CLOSED_BY_SERVER, request); 
        
        AsyncHttpClientCallback callback = request.getCallback();
        if (callback != null) {
            callback.onClosed();
        }
    }

    /**
     * Handler for notifying that a message was sent to a remote server.  It is responsible for setting up the
     * timeout for a response from the remote server.
     *
     * @param ioSession the {@link org.apache.mina.common.IoSession} representing the connection to the server.
     * @param object    the {@link HttpRequestMessage} object
     * @see org.apache.mina.common.IoHandlerAdapter#messageSent(org.apache.mina.common.IoSession,java.lang.Object)
     */
    public void messageSent(IoSession ioSession, Object object) throws Exception {
        HttpRequestMessage msg = (HttpRequestMessage) object;

        //Start the timeout timer now if a timeout is needed and there is not one already in effect for this request
        if (msg.getTimeOut() > 0 && msg.getTimeoutHandle() == null) {
            TimeoutTask task = new TimeoutTask(ioSession);
            ScheduledFuture<?> handle = scheduler.schedule(task, msg.getTimeOut(), TimeUnit.MILLISECONDS);
            msg.setTimeoutHandle(handle);
        }
    }

    /**
     * Utility function to cancel a request timeout task.
     *
     * @param request the {@link HttpRequestMessage} request
     */
    private void cancelTasks(HttpRequestMessage request) {
        ScheduledFuture<?> handle = request.getTimeoutHandle();
        if (handle != null) {
            boolean canceled = handle.cancel(true);
            //See if it canceled
            if (!canceled) {
                //Couldn't cancel it and it ran, so too late :-(
                return;
            }
            request.setTimeoutHandle(null);
        }
    }

    /**
     * The Class TimeoutTask.  Subclass that encapsulates handler for timeouts for the scheduler.
     */
    class TimeoutTask implements Runnable {

        /**
         * The session object.
         */
        private IoSession sess;

        /**
         * Instantiates a new timeout task.
         *
         * @param sess the {@link org.apache.mina.common.IoSession} representing the connection to the server.
         */
        public TimeoutTask(IoSession sess) {
            this.sess = sess;
        }

        /**
         * The running task which handles timing out the connection.
         *
         * @see java.lang.Runnable#run()
         */
        public void run() {
            // complete the future which will also fire the callback
            HttpRequestMessage request = (HttpRequestMessage)sess.getAttribute(CURRENT_REQUEST);
            
            AsyncHttpClient client = (AsyncHttpClient) sess.getAttachment();

            // notify any interesting parties that this is starting 
            client.notifyMonitoringListeners(MonitoringEvent.REQUEST_TIMEOUT, request); 
            
            ResponseFuture result = request.getResponseFuture();
            result.setException(new TimeoutException());
            
            //Close the session, its no good since the server is timing out
            sess.close();
        }

    }


}
