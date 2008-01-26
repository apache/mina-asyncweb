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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.mina.common.IoFilter;
import org.apache.mina.filter.codec.http.DefaultHttpResponse;
import org.apache.mina.filter.codec.http.HttpRequest;
import org.apache.mina.filter.codec.http.HttpResponseStatus;
import org.apache.mina.filter.codec.http.MutableHttpResponse;
import org.safehaus.asyncweb.service.resolver.ServiceResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A <code>ServiceHandler</code> which employs a <code>ServiceResolver</code>
 * to map incoming requests to an <code>HttpService</code> which is
 * then invoked.
 * If an incoming request can not be mapped to an <code>HttpService</code>,
 * a <code>404</code> response status is returned to the client
 *
 * @author irvingd
 *
 */
public class HttpServiceHandler implements HttpServiceFilter {

    private static final Logger LOG = LoggerFactory
            .getLogger(HttpServiceHandler.class);

    private ServiceResolver resolver;

    private Map<String, HttpService> serviceMap = new HashMap<String, HttpService>();

    /**
     * Adds an <code>HttpService</code> against a service name.
     * The service will be invoked this handlers associated
     * <code>ServiceResolver</code> resolves a request to the
     * specified service name.<br/>
     *
     * Any existing registration against the given name is overwritten.
     *
     * @param name         The service name
     * @param httpService  The service
     */
    public void addHttpService(String name, HttpService httpService) {
        Object oldService = serviceMap.put(name, httpService);
        if (oldService != null && LOG.isWarnEnabled()) {
            LOG.warn("Duplicate mapping for '" + name
                    + "'. Previous mapping removed");
        }
        LOG.info("New HttpService registered against key '" + name + "'");
    }

    /**
     * Associates this handler with the <code>ServiceResolver</code>
     * it is to employ
     *
     * @param resolver  The resolver to employ
     */
    public void setServiceResolver(ServiceResolver resolver) {
        LOG.info("Associated with service resolver [ " + resolver + "]");
        this.resolver = resolver;
    }

    /**
     * Attempts to resolve the specified request to an <code>HttpService</code>
     * known to this handler by employing this handlers associated
     * <code>ServiceResolver</code>.<br/>
     * If an <code>HttpService</code> is located for the request, it is provided
     * with the request. Otherwise, a <code>404</code> response is committed
     * for the request
     */
    public void handleRequest(NextFilter next, HttpServiceContext context)
            throws Exception {
        HttpService service = null;
        HttpRequest request = context.getRequest();
        String serviceName = resolver.resolveService(request);
        if (serviceName != null) {
            service = serviceMap.get(serviceName);
        }
        if (service == null) {
            handleUnmappedRequest(context);
        } else {
            if (LOG.isInfoEnabled()) {
                LOG.info("Mapped request [" + request.getRequestUri() + "] to "
                        + "service '" + serviceName + "'");
            }
            service.handleRequest(context);
            next.invoke();
        }
    }

    /**
     * Handles a response. This handler does not perform any
     * action for responses - so the specified {@link IoFilter.NextFilter} is
     * invoked immediately.
     */
    public void handleResponse(NextFilter next, HttpServiceContext context) {
        next.invoke();
    }

    /**
     * Starts this handler.
     */
    public void start() {
        LOG.info("HttpServiceHandler starting");
        for (Entry<String, HttpService> entry : serviceMap
                .entrySet()) {
         String serviceName = entry.getKey();
         HttpService service = entry.getValue();
         LOG.info("Starting HttpService '" + serviceName + "'");
         service.start();
         LOG.info("HttpService '" + serviceName + "' started");
      }
    }

    /**
     * Stops this handler
     */
    public void stop() {
        LOG.info("HttpServiceHandler stopping");
        for (Entry<String, HttpService> entry : serviceMap
                .entrySet()) {
         String serviceName = entry.getKey();
         HttpService service = entry.getValue();
         LOG.info("Stopping HttpService '" + serviceName + "'");
         service.stop();
         LOG.info("HttpService '" + serviceName + "' stopped");
      }
    }

    /**
     * Handles an unmapped request by issuing a <code>404</code>
     * response to the client
     */
    private void handleUnmappedRequest(HttpServiceContext context) {
        HttpRequest request = context.getRequest();
        if (LOG.isWarnEnabled()) {
            LOG.warn("Failed to map '" + request.getRequestUri() + "' to "
                    + "a resource");
        }
        MutableHttpResponse response = new DefaultHttpResponse();
        response.setStatus(HttpResponseStatus.NOT_FOUND);
        response.setStatusReasonPhrase(request.getRequestUri().toString());
        context.commitResponse(response);
    }
}
