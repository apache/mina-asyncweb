/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.ahc;

import java.net.URL;
import java.util.concurrent.TimeUnit;

import org.apache.ahc.auth.AuthScope;
import org.apache.ahc.auth.UsernamePasswordCredentials;
import org.apache.ahc.codec.HttpRequestMessage;

public class AuthTest extends AbstractTest {

    AsyncHttpClient ahc;

    public void testBasicAuth() throws Exception {
        TestCallback callback = new TestCallback();
        HttpRequestMessage request = new HttpRequestMessage(
            new URL("http://localhost:8282/authbasic/secure.jsp"), callback);

        UsernamePasswordCredentials creds = new UsernamePasswordCredentials("test","password");
        request.addCredentials(AuthScope.ANY, creds);
        ahc.sendRequest(request);

        callback.await(5, TimeUnit.SECONDS);

        assertEquals(200, callback.getMessage().getStatusCode());
        assertTrue(callback.getMessage().getStringContent().startsWith("Hello World!"));
    }

    public void testBasicAuthFailure() throws Exception {

        TestCallback callback = new TestCallback();
        HttpRequestMessage request = new HttpRequestMessage(
            new URL("http://localhost:8282/authbasic/secure.jsp"), callback);

        UsernamePasswordCredentials creds = new UsernamePasswordCredentials("test","badpassword");
        request.addCredentials(AuthScope.ANY, creds);
        ahc.sendRequest(request);

        callback.await(5, TimeUnit.SECONDS);

        //Should be an auth failure
        assertEquals(401, callback.getMessage().getStatusCode());
    }

    public void testDigestAuth() throws Exception {
        TestCallback callback = new TestCallback();
        HttpRequestMessage request = new HttpRequestMessage(
            new URL("http://localhost:8282/authdigest/secure.jsp"), callback);

        UsernamePasswordCredentials creds = new UsernamePasswordCredentials("test","password");
        request.addCredentials(AuthScope.ANY, creds);
        ahc.sendRequest(request);

        callback.await(5, TimeUnit.SECONDS);

        assertEquals(200, callback.getMessage().getStatusCode());
        assertTrue(callback.getMessage().getStringContent().startsWith("Hello World!"));
    }

    protected void setUp() throws Exception {
        super.setUp();
        ahc = new AsyncHttpClient();
        ahc.setTcpNoDelay(true);
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        ahc.destroyAll();
    }
}
