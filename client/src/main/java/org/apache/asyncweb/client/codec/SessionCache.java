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

import java.net.InetSocketAddress;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

import org.apache.mina.common.IoSession;

/**
 * Class that provides access to cached sessions.  IoSessions are cached using
 * the host and the port.  This class is thread safe.
 */
public final class SessionCache {
    private final ConcurrentMap<InetSocketAddress,Queue<IoSession>> cachedSessions = 
            new ConcurrentHashMap<InetSocketAddress,Queue<IoSession>>();
    
    public SessionCache() {}

    /**
     * Returns an IoSession that is connected and considered usable.  Note that
     * this is still on a best-effort basis, and there is no guarantee that the
     * connection can be used without errors, although it should be usually
     * safe to use it.
     * 
     * @param addr the remote address with which to look up an active session.
     * @throws IllegalArgumentException if a null address was passed in.
     * @return an active IoSession, or null if none are found.
     */
    public IoSession getActiveSession(InetSocketAddress addr) {
        if (addr == null) {
            throw new IllegalArgumentException("null address was passed in");
        }
        
        Queue<IoSession> queue = cachedSessions.get(addr);
        if (queue == null) {
            return null;
        }
        
        IoSession cached = null;
        while ((cached = queue.poll()) != null) {
            // see if the session is usable
            if (cached.isConnected() && !cached.isClosing()) {
                return cached;
            }
        }
        return null;
    }
    
    /**
     * Caches the given session using its remote address.
     * 
     * @param session IoSession to cache
     * @throws IllegalArgumentException if a null session was passed in.
     */
    void cacheSession(IoSession session) {
        if (session == null) {
            throw new IllegalArgumentException("null session was passed in");
        }
        
        InetSocketAddress addr = (InetSocketAddress)session.getRemoteAddress();
		Queue<IoSession> queue = cachedSessions.get(addr);
		if (queue == null) {
            queue = new ConcurrentLinkedQueue<IoSession>();
            Queue<IoSession> existing = cachedSessions.putIfAbsent(addr, queue);
            if (existing != null) {
                // the value exists
                queue = existing;
            }
        }
        // add it to the queue
        queue.offer(session);
    }
    
    /**
     * Removes the given session from the cache if it is in the cache.
     * 
     * @param session IoSession to remove from the cache
     * @throws IllegalArgumentException if a null session was passed in
     */
    void removeSession(IoSession session) {
        if (session == null) {
            throw new IllegalArgumentException("null session was passed in");
        }
        
        InetSocketAddress addr = (InetSocketAddress)session.getRemoteAddress();
        Queue<IoSession> queue = cachedSessions.get(addr);
        if (queue != null) {
            queue.remove(session);
        }
    }
}
