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

import org.safehaus.asyncweb.common.MutableHttpResponse;
import org.safehaus.asyncweb.service.HttpServiceContext;
import org.safehaus.asyncweb.service.HttpSession;

/**
 * A facade through which <code>Session</code>s are created and accessed.
 * A <code>SessionAccessor</code> provides a simple interface for accessing
 * the Session associated with a request (and optionally creating it if required)
 *
 * @author irvingd
 *
 */
public interface HttpSessionAccessor {

    /**
     * Attempts to locate the session associated with the specified context.
     * If no session can be located based on the request, and <code>create</code>
     * is <code>true</code>, a new session is created and bound against the request.
     * Otherwise, if a session is not currently bound against the specified request
     * and <code>create</code> is false, this method returns <code>null</code>
     *
     * @param context  The context for which a session is required
     * @param create   If this parameter is <code>true</code> and no session can
     *                 be found for the given request, a new session is created
     *                 and bound against the request
     * @return         The located / created session - <code>null</code> if an
     *                 existing session can not be located and <code>create</code>
     *                 is <code>false</code>
     */
    public HttpSession getSession(HttpServiceContext context, boolean create);

    /**
     * Adds session identifier to the specified response.
     */
    public void addSessionIdentifier(HttpServiceContext context,
            MutableHttpResponse response);

    /**
     * Prepares this accessor for use
     */
    public void init();

    /**
     * Disposes of this accessor - freeing all session resources
     */
    public void dispose();

}
