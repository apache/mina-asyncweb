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
import org.apache.ahc.util.CountingMonitor;
import org.apache.ahc.util.MonitoringEvent;

public class RetryTest extends AbstractTest {


    public void testHtmlConnection() throws Exception {
        TestCallback callback = new TestCallback();
        CountingMonitor counter = doGetConnection(callback, "http://localhost:8284/");
        Thread.sleep(5000); 
        assertTrue(callback.isException());
        assertTrue(counter.getCount(MonitoringEvent.CONNECTION_RETRIED) == 3); 
    }

    public void testSSLHtmlConnection() throws Exception {
        TestCallback callback = new TestCallback();
        CountingMonitor counter = doGetConnection(callback, "https://localhost:8385/");
        
        Thread.sleep(5000); 
        assertTrue(callback.isException());
        assertTrue(counter.getCount(MonitoringEvent.CONNECTION_RETRIED) == 3); 
    }

    private CountingMonitor doGetConnection(TestCallback callback, String url)
        throws Exception {
        HttpRequestMessage request = new HttpRequestMessage(new URL(url), callback);

        request.setParameter("TEST1", "Test One");
        request.setParameter("TEST2", "Test Two");
        return doConnection(request, callback);
    }          

    private CountingMonitor doConnection(HttpRequestMessage request,
                              TestCallback callback) throws Exception {
        AsyncHttpClient ahc = new AsyncHttpClient();
        ahc.setTcpNoDelay(true);
        ahc.setConnectionRetries(3); 
        CountingMonitor counter = new CountingMonitor(); 
        ahc.addMonitoringListener(counter); 
        // set a short timeout 
        ahc.setTimeout(1); 

        ahc.sendRequest(request);

        //We are done...Thread would normally end...
        //So this little wait simulates the thread going back in the pool
        //5 second timeout due to no response
        callback.await(5, TimeUnit.SECONDS);
        // and return our monitor 
        return counter; 
    }
}

