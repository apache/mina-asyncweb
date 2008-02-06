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
package org.apache.ahc;

import java.net.URL;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.ahc.codec.HttpRequestMessage;
import org.apache.ahc.codec.HttpResponseMessage;
import org.apache.ahc.codec.ConnectionPool;        

public class ConnectionReuseTest extends AbstractTest {
    // variable that keeps count of session close's
    private final AtomicInteger closeCount = new AtomicInteger(0);
    
    private final ConnectionPool connectionPool = new ConnectionPool(); 
    
    // It is important that this test case contains these methods in this order.
    // It is because to test connection reuse we need to keep the embedded 
    // server running while connections are reused.  However, AbstractTest 
    // starts and tears down the server around each test method.
    public void testConnectionReuse() throws Exception {
        // reset the count
        closeCount.set(0);
        Future<HttpResponseMessage> future = 
                submitRequest("http://localhost:8282/", true, new SessionCloseCounter());

        HttpResponseMessage msg = future.get();
        assertEquals("Hello World!", msg.getStringContent().trim());
        
        // do another request for the same host
        future = submitRequest("http://localhost:8282/params.jsp", true, 
                new SessionCloseCounter());

        msg = future.get();
        assertEquals("Test One Test Two", msg.getStringContent());
        
        // check that I got zero close at this point
        assertEquals(0, closeCount.get());
    }
    
    
    public void testSSLConnectionReuse() throws Exception {
        // reset the count
        closeCount.set(0);
        Future<HttpResponseMessage> future = 
                submitRequest("https://localhost:8383/", true, new SessionCloseCounter());

        HttpResponseMessage msg = future.get();
        assertEquals("Hello World!", msg.getStringContent().trim());
        
        // do another request for the same host
        future = submitRequest("https://localhost:8383/", true, new SessionCloseCounter());

        msg = future.get();
        assertEquals("Hello World!", msg.getStringContent().trim());
        
        // check that I got zero close at this point
        assertEquals(0, closeCount.get());
    }
    
    
    public void testConnectionClose() throws Exception {
        // reset the count
        closeCount.set(0);
        Future<HttpResponseMessage> future = 
                submitRequest("http://localhost:8282/", false, new SessionCloseCounter());

        HttpResponseMessage msg = future.get();
        assertEquals("Hello World!", msg.getStringContent().trim());
        
        // do another request for the same host
        future = submitRequest("http://localhost:8282/params.jsp", false, 
                new SessionCloseCounter());

        msg = future.get();
        assertEquals("Test One Test Two", msg.getStringContent());
        
        // give it a bit of time to catch up
        Thread.sleep(500L);
        
        // check that I got close count of 2 at this point
        assertEquals(2, closeCount.get());
    }
    
    private Future<HttpResponseMessage> submitRequest(String url, 
                                                      boolean reuseConnection,
                                                      AsyncHttpClientCallback cb) 
            throws Exception {
        HttpRequestMessage request = new HttpRequestMessage(new URL(url), cb);

        request.setParameter("TEST1", "Test One");
        request.setParameter("TEST2", "Test Two");
        AsyncHttpClient ahc = new AsyncHttpClient();
        if (reuseConnection) {
            ahc.setConnectionPool(connectionPool);  
        }
        ahc.setTcpNoDelay(true);
        return ahc.sendRequest(request);
    }
    
    private class SessionCloseCounter implements AsyncHttpClientCallback {
        public void onClosed() {
            System.out.println("onClosed()");
            // increment the counter on every close
            closeCount.incrementAndGet();
        }

        public void onException(Throwable cause) {}
        public void onResponse(HttpResponseMessage message) {}
        public void onTimeout() {}
    }
}
