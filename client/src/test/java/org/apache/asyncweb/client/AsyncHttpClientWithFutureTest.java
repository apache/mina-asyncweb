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
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.asyncweb.client.codec.HttpRequestMessage;
import org.apache.asyncweb.client.codec.HttpResponseMessage;
import org.apache.asyncweb.client.codec.ResponseFuture;

public class AsyncHttpClientWithFutureTest extends AbstractTest {
    public void testHtmlRequest() throws Exception {
        Future<HttpResponseMessage> future = submitGetRequest("http://localhost:8282/", true, null);

        HttpResponseMessage msg = future.get();
        assertEquals("\nHello World!", msg.getStringContent());
    }

    public void testSSLHtmlRequest() throws Exception {
        Future<HttpResponseMessage> future = submitGetRequest("https://localhost:8383/", true, null);

        HttpResponseMessage msg = future.get();
        assertEquals("\nHello World!", msg.getStringContent());
    }
    
    public void testMultipleRequests() throws Exception {
    	BlockingQueue<ResponseFuture> completionQueue =
    			new LinkedBlockingQueue<ResponseFuture>();
    	
    	// fire two HTTP requests on the same queue
    	submitGetRequest("http://localhost:8282/", true, completionQueue);
    	submitGetRequest("http://localhost:8282/params.jsp", true, completionQueue);
    	
    	// check the results
    	for (int i = 0; i < 2; i++) {
    		// we don't know which one will complete first
    		ResponseFuture future = completionQueue.take(); // this blocks
    		String content = future.get().getStringContent();
    		String url = future.getRequest().getUrl().toString();
    		if (url.equals("http://localhost:8282/")) {
    			assertEquals("\nHello World!", content);
    		} else {
    			assertEquals("Test One Test Two", content);
    		}
    	}
    	// the fact we reach here is a success
    	assertTrue(true);
    }

    public void testRedirect() throws Exception {
        //Test that we are following redirects
        Future<HttpResponseMessage> future = submitGetRequest("http://localhost:8282/redirect.jsp", true, null);

        HttpResponseMessage msg = future.get();
        assertEquals("\nHello World!", msg.getStringContent());

        //Test that we are not following redirects
        future = submitGetRequest("http://localhost:8282/redirect.jsp", false, null);

        msg = future.get();
        assertEquals(302, msg.getStatusCode());
        assertEquals(msg.getLocation(), "http://localhost:8282/index.jsp"); 
    }

    public void testGetParameters() throws Exception {
        Future<HttpResponseMessage> future = 
        		submitGetRequest("http://localhost:8282/params.jsp", true, null);

        HttpResponseMessage msg = future.get();
        assertEquals("Test One Test Two", msg.getStringContent());
    }

    public void testPostParameters() throws Exception {
        Future<HttpResponseMessage> future = submitPostRequest("http://localhost:8282/params.jsp", null);

        HttpResponseMessage msg = future.get();
        assertEquals("Test One Test Two", msg.getStringContent());
    }
    
    private Future<HttpResponseMessage> submitGetRequest(String url, 
    													 boolean followRedirects, 
    													 BlockingQueue<ResponseFuture> queue) 
    		throws Exception {
        HttpRequestMessage request = new HttpRequestMessage(new URL(url), null);
        request.setFollowRedirects(followRedirects);

        request.setParameter("TEST1", "Test One");
        request.setParameter("TEST2", "Test Two");
        return submitRequest(request, queue);
    }
    
    private Future<HttpResponseMessage> submitPostRequest(String url, 
    													  BlockingQueue<ResponseFuture> queue) 
    		throws Exception {
        HttpRequestMessage request = new HttpRequestMessage(new URL(url), null);
        request.setParameter("TEST1", "Test One");
        request.setParameter("TEST2", "Test Two");
        request.setRequestMethod(HttpRequestMessage.REQUEST_POST);
        return submitRequest(request, queue);
    }
    
    private Future<HttpResponseMessage> submitRequest(HttpRequestMessage request, 
    												  BlockingQueue<ResponseFuture> queue)
    		throws Exception {
    	AsyncHttpClient ahc = new AsyncHttpClient();
    	ahc.setTcpNoDelay(true);
    	return ahc.sendRequest(request, queue);
    }
}
