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
import java.util.concurrent.TimeUnit;

import org.apache.ahc.codec.HttpRequestMessage;
import org.apache.ahc.codec.HttpResponseMessage;
import org.apache.ahc.util.CountingMonitor;
import org.apache.ahc.util.MonitoringEvent;
import org.apache.ahc.util.MonitoringListener;

public class MonitoringTest extends AbstractTest {

    public void testHtmlConnection() throws Exception {
        TestCallback callback = new TestCallback();
        CountingMonitor counter = new CountingMonitor(); 
        doGetConnection(callback, "http://localhost:8282/", false, true, counter);

        HttpResponseMessage msg = callback.getMessage();
        assertEquals("Hello World!", msg.getStringContent().trim());
        
        // the monitor events are dispatched asynchronously, so give a little time 
        // for them all to be dispatched. 
        Thread.sleep(500); 
        assertEquals(counter.getCount(MonitoringEvent.REQUEST_STARTED), 1); 
        assertEquals(counter.getCount(MonitoringEvent.REQUEST_COMPLETED), 1); 
        assertEquals(counter.getCount(MonitoringEvent.REQUEST_FAILED), 0); 
        assertEquals(counter.getCount(MonitoringEvent.REQUEST_TIMEOUT), 0); 
        assertEquals(counter.getCount(MonitoringEvent.CONNECTION_ATTEMPTED), 1); 
        assertEquals(counter.getCount(MonitoringEvent.CONNECTION_RETRIED), 0); 
        assertEquals(counter.getCount(MonitoringEvent.CONNECTION_REUSED), 0); 
        assertEquals(counter.getCount(MonitoringEvent.CONNECTION_SUCCESSFUL), 1); 
        assertEquals(counter.getCount(MonitoringEvent.REQUEST_REDIRECTED), 0); 
        assertEquals(counter.getCount(MonitoringEvent.REQUEST_CHALLENGED), 0); 
        assertEquals(counter.getCount(MonitoringEvent.CONNECTION_CLOSED_BY_SERVER), 1); 
        assertEquals(counter.getCount(MonitoringEvent.CONNECTION_CLOSED), 0); 
    }

    public void testSSLHtmlConnection() throws Exception {
        TestCallback callback = new TestCallback();
        CountingMonitor counter = new CountingMonitor(); 
        doGetConnection(callback, "https://localhost:8383/", false, true, counter);

        HttpResponseMessage msg = callback.getMessage();
        assertEquals("Hello World!", msg.getStringContent().trim());
        // the monitor events are dispatched asynchronously, so give a little time 
        // for them all to be dispatched. 
        Thread.sleep(500); 
        assertEquals(counter.getCount(MonitoringEvent.REQUEST_STARTED), 1); 
        assertEquals(counter.getCount(MonitoringEvent.REQUEST_COMPLETED), 1); 
        assertEquals(counter.getCount(MonitoringEvent.REQUEST_FAILED), 0); 
        assertEquals(counter.getCount(MonitoringEvent.REQUEST_TIMEOUT), 0); 
        assertEquals(counter.getCount(MonitoringEvent.CONNECTION_ATTEMPTED), 1); 
        assertEquals(counter.getCount(MonitoringEvent.CONNECTION_RETRIED), 0); 
        assertEquals(counter.getCount(MonitoringEvent.CONNECTION_REUSED), 0); 
        assertEquals(counter.getCount(MonitoringEvent.CONNECTION_SUCCESSFUL), 1); 
        assertEquals(counter.getCount(MonitoringEvent.REQUEST_REDIRECTED), 0); 
        assertEquals(counter.getCount(MonitoringEvent.REQUEST_CHALLENGED), 0); 
        assertEquals(counter.getCount(MonitoringEvent.CONNECTION_CLOSED_BY_SERVER), 1); 
        assertEquals(counter.getCount(MonitoringEvent.CONNECTION_CLOSED), 0); 
    }

    public void testRedirect() throws Exception {
        TestCallback callback = new TestCallback();

        CountingMonitor counter = new CountingMonitor(); 
        //Test that we are following redirects
        doGetConnection(callback, "http://localhost:8282/redirect.jsp", false, true, counter);

        HttpResponseMessage msg = callback.getMessage();
        assertEquals("Hello World!", msg.getStringContent().trim());
        // the monitor events are dispatched asynchronously, so give a little time 
        // for them all to be dispatched. 
        Thread.sleep(500); 
        
        assertEquals(counter.getCount(MonitoringEvent.REQUEST_STARTED), 2); 
        assertEquals(counter.getCount(MonitoringEvent.REQUEST_COMPLETED), 1); 
        assertEquals(counter.getCount(MonitoringEvent.REQUEST_FAILED), 0); 
        assertEquals(counter.getCount(MonitoringEvent.REQUEST_TIMEOUT), 0); 
        assertEquals(counter.getCount(MonitoringEvent.CONNECTION_ATTEMPTED), 2); 
        assertEquals(counter.getCount(MonitoringEvent.CONNECTION_RETRIED), 0); 
        assertEquals(counter.getCount(MonitoringEvent.CONNECTION_REUSED), 0); 
        assertEquals(counter.getCount(MonitoringEvent.CONNECTION_SUCCESSFUL), 2); 
        assertEquals(counter.getCount(MonitoringEvent.REQUEST_REDIRECTED), 1); 
        assertEquals(counter.getCount(MonitoringEvent.REQUEST_CHALLENGED), 0); 
        assertEquals(counter.getCount(MonitoringEvent.CONNECTION_CLOSED_BY_SERVER), 2); 
        assertEquals(counter.getCount(MonitoringEvent.CONNECTION_CLOSED), 0); 
        
        counter.clearCounters(); 

        //Test that we are not following redirects
        callback = new TestCallback();
        doGetConnection(callback, "http://localhost:8282/redirect.jsp", false, false, counter);

        msg = callback.getMessage();
        assertEquals(302, msg.getStatusCode());
        assertEquals(msg.getLocation(), "http://localhost:8282/index.jsp"); 
        // the monitor events are dispatched asynchronously, so give a little time 
        // for them all to be dispatched. 
        Thread.sleep(500); 
        
        assertEquals(counter.getCount(MonitoringEvent.REQUEST_STARTED), 1); 
        assertEquals(counter.getCount(MonitoringEvent.REQUEST_COMPLETED), 1); 
        assertEquals(counter.getCount(MonitoringEvent.REQUEST_FAILED), 0); 
        assertEquals(counter.getCount(MonitoringEvent.REQUEST_TIMEOUT), 0); 
        assertEquals(counter.getCount(MonitoringEvent.CONNECTION_ATTEMPTED), 1); 
        assertEquals(counter.getCount(MonitoringEvent.CONNECTION_RETRIED), 0); 
        assertEquals(counter.getCount(MonitoringEvent.CONNECTION_REUSED), 0); 
        assertEquals(counter.getCount(MonitoringEvent.CONNECTION_SUCCESSFUL), 1); 
        assertEquals(counter.getCount(MonitoringEvent.REQUEST_REDIRECTED), 0); 
        assertEquals(counter.getCount(MonitoringEvent.REQUEST_CHALLENGED), 0); 
        assertEquals(counter.getCount(MonitoringEvent.CONNECTION_CLOSED_BY_SERVER), 1); 
            
        assertEquals(counter.getCount(MonitoringEvent.CONNECTION_CLOSED), 0); 
    }

    private void doGetConnection(TestCallback callback, String url,
                                 boolean testForException, boolean followRedirects, 
                                 MonitoringListener listener)  
        throws Exception {
        HttpRequestMessage request = new HttpRequestMessage(new URL(url), callback);
        request.setFollowRedirects(followRedirects);

        request.setParameter("TEST1", "Test One");
        request.setParameter("TEST2", "Test Two");
        doConnection(request, false, callback, listener);
    }

    private void doConnection(HttpRequestMessage request, boolean testForException, 
                              TestCallback callback, MonitoringListener listener) throws Exception {
        AsyncHttpClient ahc = new AsyncHttpClient();
        ahc.addMonitoringListener(listener); 
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

