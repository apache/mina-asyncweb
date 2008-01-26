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
package org.apache.asyncweb.server.session;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.asyncweb.server.HttpSession;
import org.safehaus.asyncweb.util.LinkedPermitIssuer;
import org.safehaus.asyncweb.util.PermitExpirationListener;
import org.safehaus.asyncweb.util.TimedPermit;
import org.safehaus.asyncweb.util.TimedPermitIssuer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple <code>SessionStore</code> implementation which holds all session
 * data in memory. A <code>TimedPermitIssuer</code> is employed to issue a time-out
 * permit for each issued session.
 *
 * @author irvingd
 *
 */
public class BasicSessionStore implements HttpSessionStore {

    private static final Logger LOG = LoggerFactory
            .getLogger(BasicSessionStore.class);

    /**
     * Default session timeout of 15 minutes
     */
    private static final long DEFAULT_SESSION_TIMEOUT = 900000;

    private Map<String, HttpSession> sessionMap = Collections
            .synchronizedMap(new HashMap<String, HttpSession>());

    private List<HttpSessionListener> listeners = Collections
            .synchronizedList(new ArrayList<HttpSessionListener>());

    private TimedPermitIssuer permitIssuer;

    private boolean isClosed;

    /**
     * Constructs with the default session timeout
     */
    public BasicSessionStore() {
        this(DEFAULT_SESSION_TIMEOUT);
    }

    /**
     * Constructs with a specified session timeout
     *
     * @param sessionTimeout  The session timeout (in ms)
     */
    public BasicSessionStore(long sessionTimeout) {
        permitIssuer = new LinkedPermitIssuer(sessionTimeout);
        permitIssuer.addPermitExpirationListener(new TimeoutListener());
        LOG.info("BasicSessionStore timeout: " + sessionTimeout + "ms");
    }

    /**
     * Adds a listener to this store
     *
     * @param listener The listener to add
     */
    public void addSessionListener(HttpSessionListener listener) {
        listeners.add(listener);
    }

    /**
     * Sets the listeners employed by this store.
     * Any existing listeners are removed
     *
     * @param listeners  The listeners to be added
     */
    public void setSessionListeners(Collection<HttpSessionListener> listeners) {
        synchronized (this.listeners) {
            this.listeners.clear();
            this.listeners.addAll(listeners);
        }
    }

    /**
     * Closes this store.
     * Our permit issuer is closed, and all sessions are destroyed.
     */
    public void close() {
        List<HttpSession> closureList = null;

        synchronized (sessionMap) {
            if (isClosed) {
                LOG.debug("Already closed");
                return;
            }
            LOG.debug("BasicSessionStore closing");
            permitIssuer.close();
            isClosed = true;
            closureList = new ArrayList<HttpSession>(sessionMap.values());
        }
        for ( HttpSession httpSession : closureList) {
         BasicSession session = (BasicSession) httpSession;
         LOG.debug("Closure: Destroying session: " + session.getId());
         session.destroy();
      }
    }

    /**
     * Creates a new session for the specified key.
     *
     * @param key  The session key
     * @return The created session, or <code>null</code> if a session is already
     *         held for the specified key
     */
    public HttpSession createSession(String key) {
        BasicSession created = null;
        synchronized (sessionMap) {
            if (isClosed) {
                throw new IllegalStateException("Store closed");
            }
            if (!sessionMap.containsKey(key)) {
                created = new BasicSession(key, this);
                sessionMap.put(key, created);
                TimedPermit permit = permitIssuer.issuePermit(created);
                created.setPermit(permit);
            }
        }
        if (created != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("New session created with key '" + key
                        + "'. Firing notifications");
            }
            fireCreated(created);
        }
        return created;
    }

    /**
     * Locates the session with the specified key.
     * If the session is found, we request it to renew its access permit.
     *
     * @param key  The key for which a session is required
     * @return     The located session, or <code>null</code> if no session with the
     *             specified key was found
     */
    public HttpSession locateSession(String key) {
        BasicSession session = (BasicSession) sessionMap.get(key);
        if (session != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Located session with key '" + key
                        + "'. Marking as accessed");
            }
            session.access();
        }
        return session;
    }

    /**
     * Invoked by a session we created when it successfully processes an expiry.
     * The session is removed from our session map, and expiry notifications are fired.
     *
     * @param session  The expired session
     */
    void sessionExpired(BasicSession session) {
        if (LOG.isDebugEnabled()) {
            LOG
                    .debug("Session has been expired. Processing notifications for '"
                            + session.getId() + "'");
        }
        sessionMap.remove(session.getId());
        fireExpiry(session);
    }

    /**
     * Invoked by a session we created when it successfully processes a destruction
     * request.
     * The session is removed from our session map, and destruction notifications
     * are fired
     *
     * @param session  The destroyed session
     */
    void sessionDestroyed(BasicSession session) {
        if (LOG.isDebugEnabled()) {
            LOG
                    .debug("Session has been destroyed. Processing notifications for '"
                            + session.getId() + "'");
        }
        sessionMap.remove(session.getId());
        fireDestroyed(session);
    }

    /**
     * Invoked when the permit associated with the specified session expires.
     * We simply request the session to expire itself.
     * If the session is not already destroyed, it will request us to fire
     * notifications on its behalf.
     *
     * @param session  The expired session
     */
    private void sessionPermitExpired(BasicSession session) {
        session.expire();
    }

    /**
     * Fires creation notification to all listeners assocaited with this store
     *
     * @param session  The expired session
     */
    private void fireCreated(HttpSession session) {
        synchronized (listeners) {
            for (HttpSessionListener listener : listeners) {
            listener.sessionCreated(session);
         }
        }
    }

    /**
     * Fires destruction notification to all listeners assocaited with this store
     *
     * @param session  The expired session
     */
    private void fireDestroyed(HttpSession session) {
        synchronized (listeners) {
            for (HttpSessionListener listener : listeners) {
            listener.sessionDestroyed(session);
         }
        }
    }

    /**
     * Fires expiry notification to all listeners assocaited with this store
     *
     * @param session  The expired session
     */
    private void fireExpiry(HttpSession session) {
        synchronized (listeners) {
            for (HttpSessionListener listener : listeners) {
            listener.sessionExpired(session);
         }
        }
    }

    /**
     * Receives notifications of timed out permits issued by this store,
     * and triggers expiry of the associated session
     *
     * @author irvingd
     *
     */
    private class TimeoutListener implements PermitExpirationListener {

        /**
         * Invoked when a permit issued for a session expires
         *
         * @param session  The session which has expired
         */
        public void permitExpired(Object session) {
            sessionPermitExpired((BasicSession) session);
        }

    }

}
