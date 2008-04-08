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

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.apache.asyncweb.client.codec.HttpRequestMessage;
import org.apache.asyncweb.client.codec.HttpResponseMessage;

public class AsyncHttpClientTest extends AbstractTest {


    public void testHtmlConnection() throws Exception {
        TestCallback callback = new TestCallback();
        doGetConnection(callback, "http://localhost:8282/", false, true);

        HttpResponseMessage msg = callback.getMessage();
        assertEquals("\nHello World!", msg.getStringContent());
    }

    public void testSSLHtmlConnection() throws Exception {
        TestCallback callback = new TestCallback();
        doGetConnection(callback, "https://localhost:8383/", false, true);

        HttpResponseMessage msg = callback.getMessage();
        assertEquals("\nHello World!", msg.getStringContent());
    }

    public void testRedirect() throws Exception {
        TestCallback callback = new TestCallback();

        //Test that we are following redirects
        doGetConnection(callback, "http://localhost:8282/redirect.jsp", false, true);

        HttpResponseMessage msg = callback.getMessage();
        assertEquals("\nHello World!", msg.getStringContent());

        //Test that we are not following redirects
        callback = new TestCallback();
        doGetConnection(callback, "http://localhost:8282/redirect.jsp", false, false);

        msg = callback.getMessage();
        assertEquals(302, msg.getStatusCode());
        assertEquals(msg.getLocation(), "http://localhost:8282/index.jsp"); 
    }

    public void testBinaryRequest() throws Exception {

        //Get the real file
        File file = new File(ROOT, "pwrd_apache.gif");
        FileInputStream fis = new FileInputStream(file);
        byte realFile[] = new byte[(int)file.length()];
        fis.read(realFile);

        TestCallback callback = new TestCallback();
        doGetConnection(callback, "http://localhost:8282/pwrd_apache.gif", false, true);

        HttpResponseMessage msg = callback.getMessage();

        assertTrue(Arrays.equals(realFile, msg.getContent()));
    }

    public void testSSLBinaryRequest() throws Exception {

        //Get the real file
        File file = new File(ROOT, "pwrd_apache.gif");
        FileInputStream fis = new FileInputStream(file);
        byte realFile[] = new byte[(int)file.length()];
        fis.read(realFile);

        TestCallback callback = new TestCallback();
        doGetConnection(callback, "https://localhost:8383/pwrd_apache.gif", false, true);

        HttpResponseMessage msg = callback.getMessage();

        assertTrue(Arrays.equals(realFile, msg.getContent()));
    }

    public void testGetParameters() throws Exception {
        TestCallback callback = new TestCallback();
        doGetConnection(callback, "http://localhost:8282/params.jsp", false, true);

        HttpResponseMessage msg = callback.getMessage();
        assertEquals("Test One Test Two", msg.getStringContent());
    }

    public void testPostParameters() throws Exception {
        TestCallback callback = new TestCallback();
        doPostConnection(callback, "http://localhost:8282/params.jsp", false);

        HttpResponseMessage msg = callback.getMessage();
        assertEquals("Test One Test Two", msg.getStringContent());
    }

    private void doGetConnection(TestCallback callback, String url,
                                 boolean testForException, boolean followRedirects)
        throws Exception {
        HttpRequestMessage request = new HttpRequestMessage(new URL(url), callback);
        request.setFollowRedirects(followRedirects);

        request.setParameter("TEST1", "Test One");
        request.setParameter("TEST2", "Test Two");
        doConnection(request, false, callback);
    }

    private void doPostConnection(TestCallback callback, String url, boolean testForException)
        throws Exception {
        HttpRequestMessage request = new HttpRequestMessage(new URL(url), callback);
        request.setParameter("TEST1", "Test One");
        request.setParameter("TEST2", "Test Two");
        request.setRequestMethod(HttpRequestMessage.REQUEST_POST);
        doConnection(request, false, callback);
    }

    private void doConnection(HttpRequestMessage request,
                              boolean testForException, 
                              TestCallback callback) throws Exception {
        AsyncHttpClient ahc = new AsyncHttpClient();
        ahc.setTcpNoDelay(true);

        ahc.sendRequest(request);

        //We are done...Thread would normally end...
        //So this little wait simulates the thread going back in the pool
        //5 second timeout due to no response
        callback.await(5, TimeUnit.SECONDS);

        if (!testForException) {
            if (((TestCallback)request.getCallback()).isException()) {
                throw new Exception(((TestCallback)request.getCallback()).getThrowable());
            }
        }

    }

}
