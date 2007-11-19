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
package org.safehaus.asyncweb.service.context;

import java.net.InetSocketAddress;

import org.apache.mina.filter.codec.http.DefaultHttpResponse;
import org.apache.mina.filter.codec.http.HttpHeaderConstants;
import org.apache.mina.filter.codec.http.HttpRequest;
import org.apache.mina.filter.codec.http.HttpResponse;
import org.apache.mina.filter.codec.http.HttpResponseStatus;
import org.apache.mina.filter.codec.http.MutableHttpResponse;
import org.safehaus.asyncweb.service.HttpServiceContext;
import org.safehaus.asyncweb.service.HttpSession;
import org.safehaus.asyncweb.service.ServiceContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A default implementation of {@link HttpServiceContext}.
 *
 * @author trustin
 * @version $Rev:167 $, $Date:2006-11-15 11:10:05 +0000 (수, 15 11월 2006) $
 */
public abstract class AbstractHttpServiceContext implements HttpServiceContext {

    private final Logger log = LoggerFactory
            .getLogger(AbstractHttpServiceContext.class);

    private final InetSocketAddress remoteAddress;

    private final HttpRequest request;

    private HttpResponse committedResponse;

    private HttpSession session;

    private boolean createdSession;

    private final ServiceContainer container;

    public AbstractHttpServiceContext(InetSocketAddress remoteAddress,
            HttpRequest request, ServiceContainer container) {
        if (remoteAddress == null) {
            throw new NullPointerException("remoteAddress");
        }
        if (request == null) {
            throw new NullPointerException("request");
        }
        if (container == null) {
            throw new NullPointerException("container");
        }

        this.remoteAddress = remoteAddress;
        this.request = request;
        this.container = container;
        this.session = container.getSessionAccessor().getSession(this, false);
    }

    public synchronized boolean isResponseCommitted() {
        return committedResponse != null;
    }

    /**
     * Commits a <code>HttpResponse</code> to this <code>Request</code>.
     *
     * @param response  The response to commit
     * @return <code>true</code> iff the response was committed
     */
    public boolean commitResponse(HttpResponse response) {
        synchronized (this) {
            if (isResponseCommitted()) {
                if (log.isDebugEnabled()) {
                    log
                            .info("Request already comitted to a response. Disposing response");
                }
                return false;
            }

            committedResponse = response;
        }

        // Add the session identifier if the session was newly created.
        if (createdSession) {
            container.getSessionAccessor().addSessionIdentifier(this,
                    (MutableHttpResponse) response);
        }

        // Only parsed requests can be formatted.
        if (getRequest().getMethod() != null) {
            container.getErrorResponseFormatter().formatResponse(getRequest(),
                    (MutableHttpResponse) response);
        }

        if (container.isSendServerHeader()) {
            ((MutableHttpResponse) response).setHeader(
                    HttpHeaderConstants.KEY_SERVER, "AsyncWeb");
        }

        // Normalize the response.
        ((MutableHttpResponse) response).normalize(getRequest());

        // Override connection header if needed.
        if (!container.getKeepAliveStrategy().keepAlive(this, response)) {
            ((MutableHttpResponse) response).setHeader(
                    HttpHeaderConstants.KEY_CONNECTION,
                    HttpHeaderConstants.VALUE_CLOSE);
        }

        boolean requiresClosure = !HttpHeaderConstants.VALUE_KEEP_ALIVE
                .equalsIgnoreCase(response
                        .getHeader(HttpHeaderConstants.KEY_CONNECTION));

        if (requiresClosure && log.isDebugEnabled()) {
            log.debug("Response status: " + response.getStatus());
            log.debug("Keep-alive strategy requires closure of "
                    + getRemoteAddress());
        }

        if (log.isDebugEnabled()) {
            log.debug("Committing a response:");
            log.debug("Status: " + response.getStatus() + ' '
                    + response.getStatusReasonPhrase());
            log.debug("Headers: " + response.getHeaders());
        }

        doWrite(requiresClosure);

        return true;
    }

    public boolean commitResponse(HttpResponseStatus status) {
        MutableHttpResponse response = new DefaultHttpResponse();
        response.setStatus(status);
        return commitResponse(response);
    }

    public synchronized HttpResponse getCommittedResponse() {
        return committedResponse;
    }

    public InetSocketAddress getRemoteAddress() {
        return remoteAddress;
    }

    public HttpRequest getRequest() {
        return request;
    }

    public HttpSession getSession() {
        return getSession(true);
    }

    public synchronized HttpSession getSession(boolean create) {
        if (session != null && !session.isValid()) {
            session = null;
        }
        if (session == null) {
            session = container.getSessionAccessor().getSession(this, create);
            if (create) {
                createdSession = true;
            }
        }

        return session;
    }

    protected abstract void doWrite(boolean requiresClosure);
}
