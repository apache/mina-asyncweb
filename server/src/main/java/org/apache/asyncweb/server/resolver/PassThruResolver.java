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

/**
 * A simple <code>ServiceResolver</code> which passes the full
 * request URI as the service name, optionally removing any leading
 * "/"
 *
 */
public class PassThruResolver implements ServiceResolver {

    private boolean removeLeadingSlash = true;

    public String resolveService(HttpRequest request) {
        if (request.getRequestUri().isAbsolute()) {
            return null;
        }

        String path = request.getRequestUri().getPath();
        int length = path.length();
        if (removeLeadingSlash && length > 0 && path.charAt(0) == '/') {
            path = length > 1 ? path.substring(1) : "";
        }
        return path;
    }

    public void setRemoveLeadingSlash(boolean removeLeadingSlash) {
        this.removeLeadingSlash = removeLeadingSlash;
    }

}
