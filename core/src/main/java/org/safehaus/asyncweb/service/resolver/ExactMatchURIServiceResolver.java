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
package org.safehaus.asyncweb.service.resolver;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.mina.filter.codec.http.HttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A <code>ServiceResolver</code> which maps request URIs to
 * service names. An exact match is required on a request URI to
 * a registered URI must be made for a service name to be resolved
 *
 * @author irvingd
 * FIXME Rename to ExactMatchPathServiceResolver
 */
public class ExactMatchURIServiceResolver implements ServiceResolver {

    private static final Logger LOG = LoggerFactory
            .getLogger(ExactMatchURIServiceResolver.class);

    private Map<String, String> serviceMap = new HashMap<String, String>();

    /**
     * Adds a mapping from a request URI to a service name.
     * Any existing mapping for the same URI is overwritten.
     *
     * @param uri          The URI
     * @param serviceName  The service name
     */
    public void addURIMapping(String uri, String serviceName) {
        String existingMapping = serviceMap.put(uri, serviceName);
        if (existingMapping != null) {
            LOG.info("Existing service [" + existingMapping + "] replaced by "
                    + "[" + serviceName + "] for URI [" + uri + "]");
        } else {
            LOG.info("Mapped [" + uri + "] to service [" + serviceName + "]");
        }
    }

    /**
     * Sets all uri - service name mappings from a given map.
     * Any existing mappings are removed
     *
     * @param map                  The map to set from
     * @throws ClassCastException  If any element (key or value) in the map
     *                             is not a <code>java.lang.String</code>
     */
    public void setMappings(Map<String, String> map) {
        serviceMap.clear();
        for (Entry<String, String> entry : map.entrySet()) {
         String key = entry.getKey();
         String value = entry.getValue();
         addURIMapping(key, value);
      }
    }

    /**
     * Attempts to resolve a service name for the specified request by
     * looking for an existing matching with the same URI as the specified
     * request.
     *
     * @param request  The request for which a service name is to be resolved
     * @return         The name of the service, or <code>null</code> if no
     *                 mapping exists for the requests URI
     */
    public String resolveService(HttpRequest request) {
        if (request.getRequestUri().isAbsolute()) {
            return null;
        }

        String path = request.getRequestUri().getPath();
        String serviceName = serviceMap.get(path);
        if (LOG.isDebugEnabled()) {
            if (serviceName == null) {
                LOG.debug("No mapping for path [" + path + "]");
            } else {
                LOG.debug("Mapped [" + path + "] to service [" + serviceName
                        + "]");
            }
        }
        return serviceName;
    }
}
