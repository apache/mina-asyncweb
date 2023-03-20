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
package org.apache.asyncweb.server.resolver;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.apache.asyncweb.common.HttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A <code>ServiceResolver</code> which maps request URIs to service names. An
 * pattern match is required on a request URI to a service name for returning
 * the matching service. Pattern are in the {@link Pattern} format.
 * <p>
 * For example : "/hello/.*" will match all the URI begining by the string
 * "/hello/".
 * </p>
 * 
 * @author The Apache MINA Project (dev@mina.apache.org)
 */
public class PatternMatchResolver implements ServiceResolver {

    private static final Logger LOG = LoggerFactory.getLogger(PatternMatchResolver.class);
    private Map<Pattern, String> serviceMap = new HashMap<Pattern, String>();

    /**
     * Adds a mapping from a request URI to a service name. Any existing mapping
     * for the same pattern is overwritten.
     * 
     * @param regexp
     *            The regular expression string {@link Pattern}
     * @param serviceName
     *            The matching service name
     */
    public void addPatternMapping(String regexp, String serviceName) {
        String existingMapping = serviceMap.put(Pattern.compile(regexp), serviceName);
        if (existingMapping != null) {
            LOG.info("Existing match '{}' replaced by '{}' for pattern '{}'",new Object[]{existingMapping, serviceName, regexp});
        } else {
            LOG.info("Mapped '{}' to service '{}'", regexp, serviceName);
        }
    }

    /**
     * Remove a mapping for a given regexp.
     * 
     * @param regexp
     *           The regular expression string to remove
     */
    public void removePatternMapping(String regexp) {
        String existingMapping = serviceMap.remove(Pattern.compile(regexp));

        if (existingMapping != null) {
            LOG.info("Removed mapping '{}' for service '{}'", regexp, existingMapping);
        } else {
            LOG.warn("Mapping '{}' wasn't found and can't be removed", regexp);
        }
        
    }

    /**
     * Sets all pattern - service name mappings from a given map. Any existing
     * mappings are removed
     * 
     * @param map
     *            The map to set from
     * @throws ClassCastException
     *             If any element (key or value) in the map is not a
     *             <code>java.lang.String</code>
     */
    public void setMappings(Map<String, String> map) {
        serviceMap.clear();
        for (Entry<String, String> entry : map.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            addPatternMapping(key, value);
        }
    }

    /**
     * Attempts to resolve a service name for the specified request by looking
     * for an existing pattern matching with the URI as the specified request.
     * 
     * @param request
     *            The request for which a service name is to be resolved
     * @return The name of the service, or <code>null</code> if no mapping
     *         pattern exists for the requests URI
     */
    public String resolveService(HttpRequest request) {
        if (request.getRequestUri().isAbsolute()) {
            return null;
        }

        String path = request.getRequestUri().getPath();

        // loop around patterns
        for (Entry<Pattern, String> entry : serviceMap.entrySet()) {
            if (entry.getKey().matcher(path).matches()) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Mapped '{}' to service '{}'", path, entry.getValue());
                }
                return entry.getValue();
            }
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("No mapping for path '{}'", path);
        }
        return null;
    }
}
