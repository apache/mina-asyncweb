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

import org.apache.asyncweb.client.codec.HttpResponseMessage;

/**
 * The Interface AsyncHttpClientCallback.  This callback that should be implemented to receive notifications
 * from the <code>AsyncHttpClient</code>.  The callback implementation is passed as a parameter to a
 * {@link org.apache.asyncweb.client.codec.HttpRequestMessage HttpRequestMessage}.  Each message should have its own 
 * callback implementation.
 * @see org.apache.asyncweb.client.codec.HttpRequestMessage
 */
public interface AsyncHttpClientCallback {
    
    /**
     * Message response event.  Occurs when the {@link AsyncHttpClient} receives a response from the server.
     * 
     * @param message the {@link org.apache.asyncweb.client.codec.HttpResponseMessage HttpRequestMessage} message response
     */
    void onResponse(HttpResponseMessage message);
    
    /**
     * Exception event. Occurs when the {@link AsyncHttpClient} receives any type of Exception from the connection
     * through the response.
     * 
     * @param cause the cause of the Exception
     */
    void onException(Throwable cause);
    
    /**
     * Closed event. Occurs when the {@link AsyncHttpClient} closes the connection.  This can occur if
     * the remote server closes the connection,after an Exception occurred, after a response is sent, or
     * during the handling of a redirect response.  This simply signals that the socket has closed.
     */
    void onClosed();
    
    /**
     * Timeout event.  Occurs when the {@link AsyncHttpClient} times out while waiting for a response from a
     * remote server.
     */
    void onTimeout();
}
