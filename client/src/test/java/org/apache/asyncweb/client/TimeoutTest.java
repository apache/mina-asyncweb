/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.apache.asyncweb.client;

import java.net.URL;
import java.util.concurrent.TimeUnit;

import org.apache.asyncweb.client.codec.HttpRequestMessage;

public class TimeoutTest extends AbstractTest {

    public void testTimeout() throws Exception {

        TestCallback callback = new TestCallback();

        AsyncHttpClient ahc = new AsyncHttpClient();
        ahc.setTcpNoDelay(true);

        HttpRequestMessage request = new HttpRequestMessage(new URL("http://localhost:8282/timeout.jsp"), callback);

        //Create a client with a one second timeout
        request.setTimeOut(1000);

        ahc.sendRequest(request);

        //We are done...Thread would normally end...
        //So this little wait simulates the thread going back in the pool
        //5 second timeout due to no response
        callback.await(5, TimeUnit.SECONDS);

        assertTrue(callback.isTimeout());

        ahc.destroyAll();
    }
}
