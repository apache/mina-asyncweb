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
package org.safehaus.asyncweb.service.session;

import org.safehaus.asyncweb.service.HttpSession;

/**
 * Creates and maintains <code>Session</code>s.
 *
 * @author irvingd
 *
 */
public interface HttpSessionStore {

    /**
     * Adds a listener to this <code>SessionStore</code>
     *
     * @param listener  The listener to be added
     */
    public void addSessionListener(HttpSessionListener listener);

    /**
     * Closes this store, releasing any resources it may be consuming
     */
    public void close();

    /**
     * Creates a new session with a specified key.
     * If the specified key is already in use, this method returns <code>null</code>
     * to indicate that an alternative key should be used
     *
     * @param key  The session key for the new session
     * @return     The created session, or <code>null</code> if the supplied key
     *             is already in use
     */
    public HttpSession createSession(String key);

    /**
     * Locates an existing session with the specified key.
     * Any store which employs session time-outs should perform the appropriate
     * action to mark the session as recently used before returning it.<br/>
     *
     * @param key  The key for which a session is required
     * @return     The session, or <code>null</code> if no session was found with
     *             the specified key
     */
    public HttpSession locateSession(String key);

}
