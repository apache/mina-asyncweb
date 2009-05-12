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
package org.apache.asyncweb.server.filter;

import org.apache.asyncweb.server.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.asyncweb.server.HttpServiceContext;
import org.apache.asyncweb.server.HttpServiceFilter;

/**
 * A <code>ServiceHandler</code> which causes any existing session associated with
 * each request it handles to be renewed.
 * This causes sessions attached to requests to be renewed per-request - even if
 * the request does not cause session access to occur.<br/>
 *
 * This handler does not need to be installed for deployments which do not employ
 * sessions
 *
 */
public class SessionKeepAliveFilter implements HttpServiceFilter
{

    private static final Logger LOG = LoggerFactory
            .getLogger(SessionKeepAliveFilter.class);

    /**
     * Handles the specified request.
     * The session associated with the current request - if any - is retrieved -
     * causing its lease to be renewed.
     */
    public void handleRequest(NextFilter next, HttpServiceContext context) {
        HttpSession session = context.getSession(false);
        if (session != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Session renewed: " + session.getId());
            }
        } else {
            LOG.debug("No session to renew");
        }
        next.invoke();
    }

    public void start() {
        // Not interested in startup
    }

    public void stop() {
        // Not interested in shutdown
    }

    /**
     * Simply moves the response forward in the chain
     */
    public void handleResponse(NextFilter next, HttpServiceContext context) {
        next.invoke();
    }
}
