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
package org.safehaus.asyncweb.service.session;

import org.safehaus.asyncweb.common.HttpRequest;
import org.safehaus.asyncweb.common.MutableHttpResponse;

/**
 * A strategy for encoding / decoding session keys information to / from
 * requests.
 *
 * @author irvingd
 *
 */
public interface HttpSessionIdentifier {

    /**
     * Attempts to extract a session key from a specified request.
     *
     * @param request  The request from which to extract a session key
     * @return         The extracted key, or <code>null</code> if the request
     *                 does not contain a session key
     */
    public String getSessionKey(HttpRequest request);

    /**
     * Adds a session key to the specified response
     *
     * @param key      The session key
     * @param response  The response
     */
    public void addSessionKey(String key, MutableHttpResponse response);
}
