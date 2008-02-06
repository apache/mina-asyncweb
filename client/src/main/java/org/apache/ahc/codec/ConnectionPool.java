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

import java.net.InetSocketAddress;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

import org.apache.mina.common.IoSession;

/**
 * Facilitates support for connection pooling by storing IoSession instances
 * that have keepAlive enabled.  Connection pooling is based on the
 * IoSession's remote host and port.
 * 
 * <p>This class is thread safe.
 */
public final class ConnectionPool {
    private final ConcurrentMap<String,Queue<IoSession>> pooledSessions = 
            new ConcurrentHashMap<String,Queue<IoSession>>();
    
    public ConnectionPool() {}

    /**
     * Returns an IoSession that is connected and considered usable.  Note that
     * this is still on a best-effort basis, and there is no guarantee that the
     * connection can be used without errors, although it should be usually
     * safe to use it.
     * 
     * @param msg the message for which to look up an active session.
     * @throws IllegalArgumentException if a null request message was passed in.
     * @return an active IoSession, or null if none are found.
     */
    public IoSession getActiveSession(HttpRequestMessage msg) {
        if (msg == null) {
            throw new IllegalArgumentException("null request was passed in");
        }
        
        Queue<IoSession> queue = pooledSessions.get(getKey(msg));
        if (queue == null) {
            return null;
        }
        
        IoSession pooled = null;
        while ((pooled = queue.poll()) != null) {
        	// see if the session is usable
            if (pooled.isConnected() && !pooled.isClosing()) {
                return pooled;
            }
        }
        return null;
    }
    
    /**
     * Places the given session in the pool of available connections based on the session's remote host and port.
     * 
     * @param session IoSession to pool
     * @throws IllegalArgumentException if a null session was passed in.
     */
    void poolConnection(IoSession session) {
        if (session == null) {
            throw new IllegalArgumentException("null session was passed in");
        }
        
        String key = getKey((InetSocketAddress)session.getRemoteAddress());
        Queue<IoSession> newQueue = new ConcurrentLinkedQueue<IoSession>();
        Queue<IoSession> queue = pooledSessions.putIfAbsent(key, newQueue);
        if (queue == null) {
            // the value was previously empty
            queue = newQueue;
        }
        // add it to the queue
        queue.offer(session);
    }
    
    /**
     * Removes the given session from the pool if it is in the pool.
     * 
     * @param session IoSession to remove from the pool
     * @throws IllegalArgumentException if a null session was passed in
     */
    void removeSession(IoSession session) {
        if (session == null) {
            throw new IllegalArgumentException("null session was passed in");
        }
        
        String key = getKey((InetSocketAddress)session.getRemoteAddress());
        Queue<IoSession> queue = pooledSessions.get(key);
        if (queue != null) {
            queue.remove(session);
        }
    }
    
    /**
     * Generate a request key from an HTTP request message.
     * 
     * @param msg    The request message we need a key from.
     * 
     * @return A String key instance for this request. 
     */
    private String getKey(HttpRequestMessage msg) {
        return getKey(msg.getHost(), msg.getPort());
    }
    
    /**
     * Generate a session key from an InetSocketAddress 
     * 
     * @param remote The endpoint address of the connection.
     * 
     * @return A string key for this endpoint. 
     */
    private String getKey(InetSocketAddress remote) {
        return getKey(remote.getHostName(), remote.getPort());
    }
    
    /**
     * The key is of the form "host:port".
     */
    private String getKey(String host, int port) {
        return new StringBuilder(host).append(':').append(port).toString();
    }
}
