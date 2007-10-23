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
package org.safehaus.asyncweb.service;

import java.net.InetSocketAddress;

import org.safehaus.asyncweb.common.HttpRequest;
import org.safehaus.asyncweb.common.HttpResponse;
import org.safehaus.asyncweb.common.HttpResponseStatus;


/**
 * Provides conversational context between a HTTP client and a {@link HttpService}.
 * 
 * @author trustin
 */
public interface HttpServiceContext {
  
  /**
   * Returns the socket address of the client (or last proxy).
   */
  InetSocketAddress getRemoteAddress();

  /**
   * Returns the request which is received from the client.
   */
  HttpRequest getRequest();
  
  /**
   * Returns <tt>true</tt> if a response for the request is committed.
   */
  boolean isResponseCommitted();
  
  /**
   * @return  The <code>Response</code> committed to this <code>Request</code>,
   *          or <code>null</code> if no response has been comitted
   */
  HttpResponse getCommittedResponse();
  
  /**
   * Writes the specified response back to the client.
   * The response <i>must not</i> be modified after it has been
   * submitted for comittment - irrespective of whether a commit
   * is successful or not.
   * <p>
   * A request may have only one response committed to it. The return
   * value of this method can be used to determine whether the
   * supplied response will be used as the single response for this
   * request.
   * <p>
   * Application code must not modify a response in any way after it
   * has been committed to a request. The results of doing so are 
   * undefined.
   * 
   * @param  response  The response to provide
   * @return <code>true</code> if the response was accepted
   */
  boolean commitResponse(HttpResponse response);

  /**
   * Commits a default response with a specified {@link HttpResponseStatus}.
   * 
   * @return        <code>true</code> if the response was accepted
   */
  boolean commitResponse(HttpResponseStatus status);

  /**
   * Returns the {@link HttpSession} which is associated with the client.
   * If no session is currently associated with the client, a new session is created.
   * 
   * @return  The session associated with the client
   */
  HttpSession getSession();
  
  /**
   * Returns the <code>Session</code> associated with this request.
   * 
   * @param create  If <code>true</code>, a new session is created if no session is currently
   *                associated with the client.
   * @return        The {@link HttpSession}, or <code>null</code> if no session
   *                is associated with the client and <code>create</code> is
   *                <code>false</code>
   */ 
  HttpSession getSession(boolean create);
}
