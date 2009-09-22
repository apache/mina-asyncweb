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

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.asyncweb.client.AsyncHttpClient;
import org.apache.asyncweb.client.AsyncHttpClientCallback;
import org.apache.asyncweb.client.auth.AuthChallengeParser;
import org.apache.asyncweb.client.auth.AuthPolicy;
import org.apache.asyncweb.client.auth.AuthScheme;
import org.apache.asyncweb.client.auth.AuthState;
import org.apache.asyncweb.client.util.DaemonThreadFactory;
import org.apache.asyncweb.client.util.MonitoringEvent;
import org.apache.asyncweb.client.util.NameValuePair;
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
     * The Constant REQUEST_OUTSTANDING that indicates a request is outstanding
     * on the session and no response has been received nor an exception 
     * occurred.
     */
    public static final String REQUEST_OUTSTANDING = "REQUEST_OUTSTANDING";
    
    /**
     * Indicates whether the request is getting closed due to problems.
     */
    public static final String CLOSE_PENDING = "CLOSE_PENDING";
    
    /**
     * The Constant for the proxy connect status.
     */
    public static final String PROXY_CONNECT_IN_PROGRESS = "PROXY_CONNECT_IN_PROGRESS";
    
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
        this(Executors.newSingleThreadScheduledExecutor(new DaemonThreadFactory()));
    }

    /**
     * Instantiates a new HttpIoHandler with the supplied scheduler.  It is the
     * caller's responsibility to dispose of the scheduler.
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
        // clear the request outstanding flag on the session
        ioSession.removeAttribute(REQUEST_OUTSTANDING);

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
            // we also need to clear out the parameters and the content
            request.clearAllParameters();
            request.clearContent();
            // make sure to add the cookies from the response
            request.addCookies(response.getCookies());

            //Send the redirect
            client.sendRequest(request);

            // if we've been provided with a cache, put this session into 
            // the cache. 
            SessionCache cache = getSessionCache(); 
            if (cache != null && !HttpDecoder.CLOSE.equals(response.getConnection())) {
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
                if (cache != null && !HttpDecoder.CLOSE.equals(response.getConnection())) {
                    // cache the session before we return
                    cache.cacheSession(ioSession);
                }

                // make sure to add the cookies from the response
                request.addCookies(response.getCookies());
                request.setAuthCount(authCount);
                
                client.sendRequest(request);

                return;
            }
        }

        cancelTasks(request);

        // complete the future which will also fire the callback
        ResponseFuture result = request.getResponseFuture();
        result.set(response);
        
        // notify any interesting parties that this is starting 
        client.notifyMonitoringListeners(MonitoringEvent.REQUEST_COMPLETED, request); 

        // if we've been provided with a cache, put this session into 
        // the cache. 
        SessionCache cache = getSessionCache(); 
        if (cache != null && !HttpDecoder.CLOSE.equals(response.getConnection())) {
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
        // mark the session as closing so it won't be used
        ioSession.setAttribute(CLOSE_PENDING);
    	
        // clear the request outstanding flag on the session if set
        ioSession.removeAttribute(REQUEST_OUTSTANDING);
        
        //Clean up if any in-proccess decoding was occurring
        ioSession.removeAttribute(CURRENT_RESPONSE);

        HttpRequestMessage request = (HttpRequestMessage) ioSession.getAttribute(CURRENT_REQUEST);
        cancelTasks(request);
        
        // complete the future which will also fire the callback
        ResponseFuture result = request.getResponseFuture();
        result.setException(throwable);
        
        AsyncHttpClient client = (AsyncHttpClient) ioSession.getAttachment();
        // notify any interesting parties that this is starting 
        client.notifyMonitoringListeners(MonitoringEvent.REQUEST_FAILED, request); 

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
        // clear and get the request outstanding flag on the connection
        Object requestOutstanding = ioSession.removeAttribute(REQUEST_OUTSTANDING);
        
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
        
        // if the session is closing while the request is outstanding, it means
        // the connection is closing prematurely; we need to cause an exception
        if (requestOutstanding != null) {
            request.getResponseFuture().setException(new IOException("connection was closed prematurely"));
        } else {
            // normal connection close after connection idle
            AsyncHttpClientCallback callback = request.getCallback();
            if (callback != null) {
                callback.onClosed();
            }
        }
        
        AsyncHttpClient client = (AsyncHttpClient) ioSession.getAttachment();
        // notify any interesting parties that this is starting 
        client.notifyMonitoringListeners(MonitoringEvent.CONNECTION_CLOSED_BY_SERVER, request); 
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
        // indicate there is an outstanding request on the connection
        ioSession.setAttribute(REQUEST_OUTSTANDING);
        
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
        ScheduledFuture<?> handle = request.removeTimeoutHandle();
        if (handle != null) {
            // cancel but don't interrupt
            handle.cancel(false);
        }
    }

    /**
     * The Class TimeoutTask.  Subclass that encapsulates handler for timeouts for the scheduler.
     */
    class TimeoutTask implements Runnable {

        /**
         * The session object.
         */
        private final IoSession sess;

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
            // first, indicate that the request has timed out
            sess.setAttribute(CLOSE_PENDING);
        	
        	// clear the request outstanding flag on the session
        	sess.removeAttribute(REQUEST_OUTSTANDING);
        	
            // complete the future which will also fire the callback
            HttpRequestMessage request = (HttpRequestMessage)sess.getAttribute(CURRENT_REQUEST);
            // make sure to remove the timeout handle
            request.setTimeoutHandle(null);
            
            ResponseFuture result = request.getResponseFuture();
            result.setException(new TimeoutException());
            
            AsyncHttpClient client = (AsyncHttpClient) sess.getAttachment();
            // notify any interesting parties that this is starting 
            client.notifyMonitoringListeners(MonitoringEvent.REQUEST_TIMEOUT, request); 
            
            //Close the session, its no good since the server is timing out
            sess.close();
        }

    }


}
