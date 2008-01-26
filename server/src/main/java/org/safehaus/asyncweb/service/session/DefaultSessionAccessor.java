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

import org.apache.asyncweb.common.MutableHttpResponse;
import org.safehaus.asyncweb.service.HttpServiceContext;
import org.safehaus.asyncweb.service.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple <code>SessionAccessor</code> implementation which acts as a facade
 * to an employed <code>SessionIdentifier</code>, <code>SessionKeyFactory</code>
 * and <code>SessionStore</code>.<br/>
 *
 * A Default identifier and key factory is employed by this accessor, but the
 * implementations used can be switched (if required) using the appropriate
 * setter methods.<br/>
 *
 * @author irvingd
 *
 */
public class DefaultSessionAccessor implements HttpSessionAccessor {

    private static final Logger LOG = LoggerFactory
            .getLogger(DefaultSessionAccessor.class);

    private HttpSessionIdentifier identifier = new CookieIdentifier();

    private HttpSessionKeyFactory keyFactory;

    private HttpSessionStore store;

    /**
     * Constructs with the default identifier and key factory
     */
    public DefaultSessionAccessor() {
        SecureRandomKeyFactory secureKeyFactory = new SecureRandomKeyFactory();
        secureKeyFactory.start();
        keyFactory = secureKeyFactory;
    }

    public HttpSession getSession(HttpServiceContext context, boolean create) {
        String sessionKey = identifier.getSessionKey(context.getRequest());
        HttpSession session = null;
        if (sessionKey != null) {
            LOG.debug("Request contains session key - attempting to lookup");
            session = store.locateSession(sessionKey);
            if (session == null) {
                LOG.debug("No session found with request's session key");
            }
        }
        if (session == null && create) {
            LOG
                    .debug("No existing session found for request - creating new session");
            session = createNewSession();
        }
        return session;
    }

    public void addSessionIdentifier(HttpServiceContext context,
            MutableHttpResponse response) {
        HttpSession session = context.getSession(false);
        if (session == null) {
            return;
        }
        identifier.addSessionKey(session.getId(), response);
    }

    /**
     * Sets the <code>SessionIdentifier</code> used for encoding / decoding
     * session keys to / from requests.
     * By default, a <code>CookieIdentifier</code> is employed
     *
     * @param identifier  The identifier to be employed
     */
    public void setSessionIdentifier(HttpSessionIdentifier identifier) {
        this.identifier = identifier;
    }

    /**
     * Sets the <code>SessionKeyFactory</code> employed by this accessor for
     * creating new session keys.
     * By default, a <code>SecureRandomKeyFactory</code> is employed
     *
     * @param keyFactory  The key factory to be employed
     */
    public void setSessionKeyFactory(HttpSessionKeyFactory keyFactory) {
        this.keyFactory = keyFactory;
    }

    /**
     * Sets the session store employed by this accessor
     *
     * @param store  The store
     */
    public void setSessionStore(HttpSessionStore store) {
        if (this.store != null) {
            this.store.close();
        }
        this.store = store;
    }

    /**
     * Disposes of this accessor. We simply close our store
     */
    public void dispose() {
        store.close();
    }

    /**
     * Initialises this accessor. If we have not been configured with a session store,
     * we simply employ a <code>BasicSessionStore</code> with a default time out
     */
    public void init() {
        if (store == null) {
            LOG
                    .info("No session store configured. Employing default session store");
            store = new BasicSessionStore();
        }
    }

    /**
     * Establishes a new session with the specified response.
     * We employ our <code>SessionKeyFactory</code> to generate a new session
     * key, and request our <code>SessionStore</code> to create a session based
     * on this id.<br/>
     * Our key factory is designed to not provide duplicate keys, but if this occurs
     * we cycle until an unused key is located.
     */
    private HttpSession createNewSession() {
        HttpSession session;
        String sessionKey;
        do {
            sessionKey = keyFactory.createSessionKey();
            session = store.createSession(sessionKey);
            if (session == null) {
                LOG.warn("SessionKeyFactory is providing duplicate keys!!");
            }
        } while (session == null);
        LOG.debug("New session created");
        return session;
    }
}
