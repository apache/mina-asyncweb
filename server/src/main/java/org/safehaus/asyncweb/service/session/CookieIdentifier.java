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

import org.apache.asyncweb.common.Cookie;
import org.apache.asyncweb.common.DefaultCookie;
import org.apache.asyncweb.common.HttpRequest;
import org.apache.asyncweb.common.MutableCookie;
import org.apache.asyncweb.common.MutableHttpResponse;

/**
 * A <code>SessionIdentifier</code> which adds and extracts session key
 * cookies
 *
 * @author irvingd
 *
 */
public class CookieIdentifier implements HttpSessionIdentifier {

    /**
     * The name of the cookie
     */
    private static final String SESSION_ID_COOKIE = "sessionKey";

    private String cookieId = SESSION_ID_COOKIE;

    /**
     * Sets the name of the cookie used for holding session keys.
     * The default cookie name is <code>sessionKey</code>
     *
     * @param cookieId  The cookie name to be used
     */
    public void setCookieId(String cookieId) {
        this.cookieId = cookieId;
    }

    /**
     * Extracts a session key from the session cookie supplied with the request -
     * if any
     *
     * @param  request  The request
     * @return The session key, or null if a session cookie was not located
     */
    public String getSessionKey( HttpRequest request) {
        Cookie sessionCookie = null;
        for (Cookie c : request.getCookies()) {
            if (c.getName().equals(cookieId)) {
                sessionCookie = c;
                break;
            }
        }
        return sessionCookie == null ? null : sessionCookie.getValue();
    }

    /**
     * Adds a session cookie to the specified request
     *
     * @param key      The session key
     * @param response The response
     */
    public void addSessionKey(String key, MutableHttpResponse response) {
        MutableCookie sessionCookie = new DefaultCookie(cookieId);
        sessionCookie.setMaxAge(-1); // "non-persistent"
        sessionCookie.setValue(key);
        // TODO: Set "isSecure" based on whether the request came in over
        //       a secure transport
        response.addCookie(sessionCookie);
    }
}
