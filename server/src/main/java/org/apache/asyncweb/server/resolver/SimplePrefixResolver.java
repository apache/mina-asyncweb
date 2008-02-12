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

import org.apache.asyncweb.common.HttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A very simple resolver which simply uses the full URI after
 * stripping an optional prefix as the name of the service.
 *
 * <code>SimplePrefixResolver</code> is useful when a very
 * naming scheme is used - and allows services to be resolved
 * "dynamically" without any global configuration.<br/>
 * </br>
 * Request URIs which do not begin with the configured prefix are
 * not resolved.<br/>
 * <br/>
 * For example, suppose all <code>HttpService</code>s are addressed
 * under the prefix <code>/services/</code>. We would then map as
 * follows for the following URIs:<br/>
 * <br/>
 * <table border="1" cellpadding="2">
 *  <tr><td>URI</td><td>Service Name</td></tr>
 *  <tr><td>/services/serviceA</td><td>serviceA</td></tr>
 *  <tr><td>/services/serviceB</td><td>serviceB</td></tr>
 *  <tr><td>/services/x/serviceX</td><td>x/serviceA</td></tr>
 *  <tr><td>/x/serviceA</td><td>null</td></tr>
 * </table>
 *
 * @author irvingd
 *
 */
public class SimplePrefixResolver implements ServiceResolver {

    private static final Logger LOG = LoggerFactory
            .getLogger(SimplePrefixResolver.class);

    // FIXME Rename to pathPrefix.
    private String uriPrefix;

    /**
     * Sets the prefix associated with this resolver.
     * URIs which begin with the specified prefix are resolved to the URI
     * with the prefix stripped. URIs which do not begin with the specified
     * prefix are not resolved.<br/>
     * <br/>
     * If a prefix is not set, requests are resolved to their URI value.
     *
     * @param uriPrefix  The uri prefix to apply
     */
    public void setUriPrefix(String uriPrefix) {
        this.uriPrefix = uriPrefix;
        LOG.info("URI prefix: " + uriPrefix);
    }

    /**
     * Resolves the name of the service to be employed for the specified request.
     * If this resolver is not configured with a prefix, the request resoves to
     * the request URI.<br/>
     * Otherwise, if the request URI begins with the configured prefix, the request
     * resolves to the URI with the prefix stripped. If the request URI does not
     * begin with the configured prefix, the request is unresolved
     *
     * @param request The request to resolve to a service name
     * @return        The resolved service name, or <code>null</code> if
     *                the request is un-resolved
     */
    public String resolveService( HttpRequest request) {
        if (request.getRequestUri() == null
                || request.getRequestUri().isAbsolute()) {
            return null;
        }

        String path = request.getRequestUri().getPath();
        if (uriPrefix != null && path != null) {
            if (path.startsWith(uriPrefix)) {
                path = path.substring(uriPrefix.length());
            } else {
                path = null;
            }
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Resolved request to service name: " + path);
        }
        return path;
    }

}
