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
package org.safehaus.asyncweb.service.filter.resolver;

import java.util.HashMap;
import java.util.Map;

import org.safehaus.asyncweb.common.HttpRequest;
import org.safehaus.asyncweb.service.resolver.ServiceResolver;

/**
 * A simple <code>ServiceResolver</code> which allows mappings
 * from requests to service names to be registered
 *
 * @author irvingd
 *
 */
public class DummyResolver implements ServiceResolver {

    private Map<HttpRequest, String> serviceMap = new HashMap<HttpRequest, String>();

    /**
     * Adds a mapping from a request to a service name
     *
     * @param request  The request
     * @param service  The service name
     */
    public void addMapping(HttpRequest request, String service) {
        serviceMap.put(request, service);
    }

    /**
     * Returns the service name mapped to the specified request,
     * or <code>null</code> if no mapping has been made for the
     * request
     *
     * @param  request  The request to resolve
     * @return The mapped service, or <code>null</code> if no mapping
     *         exists for the request
     */
    public String resolveService(HttpRequest request) {
        return serviceMap.get(request);
    }

}
