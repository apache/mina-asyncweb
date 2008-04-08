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

import java.net.URL;
import java.util.Date;

/**
 * A CookiePolicy implementation that resembles a common browser behavior.  It
 * uses the domain, path, max-age (or expires), secure attributes, as well as
 *  the cookie value to examine whether the cookie is applicable for the 
 *  request.
 */
public class DefaultCookiePolicy implements CookiePolicy {
    private final CookiePolicy[] policies = {
            new DomainPolicy(),
            new PathPolicy(),
            new ExpiresPolicy(),
            new SecurePolicy(),
            new EmptyValuePolicy()
    };

    public boolean accept(Cookie cookie, URL url) {
        for (CookiePolicy policy : policies) {
            if (!policy.accept(cookie, url)) {
                return false;
            }
        }
        return true;
    }
    
    public boolean matches(Cookie cookie, URL url) {
        for (CookiePolicy policy : policies) {
            if (!policy.matches(cookie, url)) {
                return false;
            }
        }
        return true;
    }
}

/**
 * Evaluates the domain attribute.
 */
class DomainPolicy implements CookiePolicy {
    /**
     * Returns whether the domain matches the request.  Returns true if the 
     * domain attribute is not empty, and the domain matches the request host
     * literally or via domain match.
     */
    public boolean matches(Cookie cookie, URL url) {
        String domain = cookie.getDomain();
        if (domain == null || domain.length() == 0) {
            // domain is a required attribute
            return false;
        }
        
        // make sure to add a leading dot
        // this is to prevent matching "foo.com" against "badfoo.com"
        if (!domain.startsWith(".")) {
            domain = "." + domain;
        }
        
        String host = url.getHost();
        // handle a direct match (i.e. domain is a FQDN)
        if (domain.substring(1).equalsIgnoreCase(host)) {
            return true;
        }
        
        // now match it as a domain
        return host.endsWith(domain);
    }
    
    /**
     * Same as <tt>matches()</tt>.
     */
    public boolean accept(Cookie cookie, URL url) {
        return matches(cookie, url);
    }
}

/**
 * Evaluates the path attribute.
 */
class PathPolicy implements CookiePolicy {
    /**
     * Returns whether the path attribute matches the request.
     */
    public boolean matches(Cookie cookie, URL url) {
        String cookiePath = cookie.getPath();
        if (cookiePath == null || cookiePath.length() == 0) {
            // path is required
            return false;
        }
        
        // strip the ending "/" if it's not a simple "/"
        if (!cookiePath.equals("/") && cookiePath.endsWith("/")) {
            cookiePath = cookiePath.substring(0, cookiePath.length()-1);
        }
        
        String requestPath = url.getPath();
        if (requestPath == null || requestPath.length() == 0) {
            requestPath = "/";
        }
        // request path should be prefixed with the cookie path attribute
        if (!requestPath.startsWith(cookiePath)) {
            return false;
        }
        
        // check the case like matching "/foo" aginst "/foobad"
        if (!cookiePath.equals("/") && !requestPath.equals(cookiePath)) {
            // the first character after the match should be '/'
            return requestPath.charAt(cookiePath.length()) == '/';
        }
        
        // match
        return true;
    }
    
    /**
     * Same as <tt>matches()</tt>.
     */
    public boolean accept(Cookie cookie, URL url) {
        return matches(cookie, url);
    }
}

/**
 * Evaluates the expiration attribute.
 */
class ExpiresPolicy implements CookiePolicy {
    /**
     * Returns whether the cookie is expired.  Returns true if it is a session
     * cookie.
     */
    public boolean matches(Cookie cookie, URL url) {
        Date expires = cookie.getExpires();
        if (expires == null) {
            // it is a session cookie; accept it
            return true;
        }
        
        return expires.after(new Date());
    }
    
    /**
     * Expiration should not be used when accepting.  Expired cookies may be
     * used to clear existing cookies.
     */
    public boolean accept(Cookie cookie, URL url) {
        return true;
    }
}

/**
 * Evaluates the secure attribute.
 */
class SecurePolicy implements CookiePolicy {
    public boolean matches(Cookie cookie, URL url) {
        return !cookie.isSecure() || url.getProtocol().equals("https");
    }
    
    /**
     * Same as <tt>matches()</tt>.
     */
    public boolean accept(Cookie cookie, URL url) {
        return matches(cookie, url);
    }
}

class EmptyValuePolicy implements CookiePolicy {
    public boolean matches(Cookie cookie, URL url) {
        String value = cookie.getValue();
        return value != null && value.length() > 0;
    }
    
    /**
     * We should accept empty cookies as they are used to clear existing 
     * cookies.
     */
    public boolean accept(Cookie cookie, URL url) {
        return true;
    }
}
