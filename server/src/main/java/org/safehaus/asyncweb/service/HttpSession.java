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
package org.safehaus.asyncweb.service;

/**
 * Provides a mechanism to store data across multiple requests from the same client.
 *
 * @author irvingd
 *
 */
public interface HttpSession {

    /**
     * @return  This sessions identifier
     */
    public String getId();

    /**
     * Returns the value bound to this session with the specified key, if any.
     *
     * @param key  The key for which the bound session value is required
     * @return     The session value bound against the specified key, or <code>null</code>
     *             if no such value exists
     */
    public Object getValue(String key);

    /**
     * Binds a value against a specified key.
     * Any value currently held for the given key is unbound.
     *
     * @param key    The key against which the session value is to be bound
     * @param value  The session value to bind
     */
    public void setValue(String key, Object value);

    /**
     * Removes a session value from this session.
     * If a value is currently bound against the specified key, it is unbound and
     * returned. If no value is bound against the given key, this method returns
     * <code>null</code>
     *
     * @param  key  The key for which the existing binding - if any - is to be removed
     * @return The removed value - or <code>null</code> if no value was bound against
     *         the specified key
     */
    public Object removeValue(String key);

    /**
     * Determines whether the client is aware of this session and has opted-in
     * to using it.
     * For newly created sessions, this method will always return <code>false</code>.
     *
     * @return <code>true</code> if the client is aware of this session, and is using it
     */
    public boolean isAttached();

    /**
     * Determines whether this session is valid for use.
     *
     * @return  <code>true</code> if this session is neither destroyed nor timed out
     */
    public boolean isValid();

    /**
     * Destroys this session, releasing any resources it may be consuming.
     * Further client requests using the same session identifier will no longer
     * be associated with this <code>Session</code>
     */
    public void destroy();

}
