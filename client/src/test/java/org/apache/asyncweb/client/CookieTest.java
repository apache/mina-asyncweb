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
package org.apache.asyncweb.client;

import java.net.URL;
import java.util.Collection;
import java.util.Date;

import junit.framework.TestCase;

import org.apache.asyncweb.client.codec.Cookie;
import org.apache.asyncweb.client.codec.DefaultCookiePolicy;
import org.apache.asyncweb.client.codec.HttpRequestMessage;

public class CookieTest extends TestCase {
    public void testCookieDomainFQDNMatch() throws Exception {
        HttpRequestMessage request = new HttpRequestMessage(new URL("http://www.foo.com"), null);
        request.setCookiePolicy(new DefaultCookiePolicy());

        Cookie cookie = new Cookie("name", "value");
        cookie.setDomain("www.foo.com");
        cookie.setPath("/");
        request.addCookie(cookie);
        
        assertMatch(request, cookie);
    }
    
    public void testCookieDomainMatch() throws Exception {
        HttpRequestMessage request = new HttpRequestMessage(new URL("http://abc.xyz.foo.com"), null);
        request.setCookiePolicy(new DefaultCookiePolicy());

        Cookie cookie = new Cookie("name", "value");
        cookie.setDomain(".foo.com");
        cookie.setPath("/");
        request.addCookie(cookie);
        
        assertMatch(request, cookie);
    }
    
    public void testCookieDomainNonMatch() throws Exception {
        HttpRequestMessage request = new HttpRequestMessage(new URL("http://www.badfoo.com"), null);
        request.setCookiePolicy(new DefaultCookiePolicy());

        Cookie cookie = new Cookie("name", "value");
        cookie.setDomain("foo.com");
        cookie.setPath("/");
        request.addCookie(cookie);
        
        assertNoMatch(request);
    }
    
    public void testCookiePathTopLevelMatch() throws Exception {
        HttpRequestMessage request = new HttpRequestMessage(new URL("http://www.foo.com"), null);
        request.setCookiePolicy(new DefaultCookiePolicy());

        Cookie cookie = new Cookie("name", "value");
        cookie.setDomain(".foo.com");
        cookie.setPath("/");
        request.addCookie(cookie);
        
        assertMatch(request, cookie);
    }
    
    public void testCookiePathMatch() throws Exception {
        HttpRequestMessage request = new HttpRequestMessage(new URL("http://www.foo.com/foo/bar?zoh"), null);
        request.setCookiePolicy(new DefaultCookiePolicy());

        Cookie cookie = new Cookie("name", "value");
        cookie.setDomain(".foo.com");
        cookie.setPath("/foo");
        request.addCookie(cookie);
        
        assertMatch(request, cookie);
    }
    
    public void testCookiePathNonMatch() throws Exception {
        HttpRequestMessage request = new HttpRequestMessage(new URL("http://www.foo.com/foobad"), null);
        request.setCookiePolicy(new DefaultCookiePolicy());

        Cookie cookie = new Cookie("name", "value");
        cookie.setDomain(".foo.com");
        cookie.setPath("/foo");
        request.addCookie(cookie);
        
        assertNoMatch(request);
    }
    
    public void testFreshCookie() throws Exception {
        HttpRequestMessage request = new HttpRequestMessage(new URL("http://www.foo.com/"), null);
        request.setCookiePolicy(new DefaultCookiePolicy());

        Cookie cookie = new Cookie("name", "value");
        cookie.setDomain(".foo.com");
        cookie.setPath("/");
        cookie.setExpires(new Date(System.currentTimeMillis() + 60*1000L));
        request.addCookie(cookie);
        
        assertMatch(request, cookie);
    }
    
    public void testExpiredCookie() throws Exception {
        HttpRequestMessage request = new HttpRequestMessage(new URL("http://www.foo.com/"), null);
        request.setCookiePolicy(new DefaultCookiePolicy());

        Cookie cookie = new Cookie("name", "value");
        cookie.setDomain(".foo.com");
        cookie.setPath("/");
        cookie.setExpires(new Date(System.currentTimeMillis() - 60*1000L));
        request.addCookie(cookie);

        assertNoMatch(request);
    }
    
    public void testSessionCookie() throws Exception {
        HttpRequestMessage request = new HttpRequestMessage(new URL("http://www.foo.com/"), null);
        request.setCookiePolicy(new DefaultCookiePolicy());

        Cookie cookie = new Cookie("name", "value");
        cookie.setDomain(".foo.com");
        cookie.setPath("/");
        request.addCookie(cookie);

        assertMatch(request, cookie);
    }

    public void testCookieSecureMatch() throws Exception {
        HttpRequestMessage request = new HttpRequestMessage(new URL("https://www.foo.com/"), null);
        request.setCookiePolicy(new DefaultCookiePolicy());

        Cookie cookie = new Cookie("name", "value");
        cookie.setDomain(".foo.com");
        cookie.setPath("/");
        cookie.setSecure(true);
        request.addCookie(cookie);
        
        assertMatch(request, cookie);
    }
    
    public void testCookieSecureNonMatch() throws Exception {
        HttpRequestMessage request = new HttpRequestMessage(new URL("http://www.foo.com/"), null);
        request.setCookiePolicy(new DefaultCookiePolicy());

        Cookie cookie = new Cookie("name", "value");
        cookie.setDomain(".foo.com");
        cookie.setPath("/");
        cookie.setSecure(true);
        request.addCookie(cookie);
        
        assertNoMatch(request);
    }
    
    public void testEmptyCookieNonMatch() throws Exception {
        HttpRequestMessage request = new HttpRequestMessage(new URL("http://www.foo.com/"), null);
        request.setCookiePolicy(new DefaultCookiePolicy());

        Cookie cookie = new Cookie("name", "");
        cookie.setDomain(".foo.com");
        cookie.setPath("/");
        request.addCookie(cookie);
        
        assertNoMatch(request);
    }
    
    private void assertMatch(HttpRequestMessage request, Cookie cookie) {
        Collection<Cookie> cookies = request.getCookies();
        assertTrue(cookies.size() == 1);
        assertEquals(cookie, cookies.iterator().next());
    }
    
    private void assertNoMatch(HttpRequestMessage request) {
        assertTrue(request.getCookies().isEmpty());
    }
}
