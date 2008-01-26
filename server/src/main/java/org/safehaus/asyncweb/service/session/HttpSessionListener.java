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
 * Receives notifications of session lifecycle events
 *
 * @author irvingd
 *
 */
public interface HttpSessionListener {

    /**
     * Invoked when a new session is created
     *
     * @param session  The created session
     */
    public void sessionCreated(HttpSession session);

    /**
     * Invoked when a session is destroyed before it expires
     *
     * @param session  The destroyed session
     */
    public void sessionDestroyed(HttpSession session);

    /**
     * Invoked when a session expires before being manually destroyed
     *
     * @param session  The expired session
     */
    public void sessionExpired(HttpSession session);

}
