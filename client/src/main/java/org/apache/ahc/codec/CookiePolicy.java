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
package org.apache.ahc.codec;

import java.net.URL;

/**
 * The interface that defines whether the cookie can be associated with the
 * request.
 */
public interface CookiePolicy {
    /**
     * Returns whether the cookie can be accepted for the given URL.  Not all
     * cookies that are accepted can be sent to the server; e.g. an expired
     * cookie.  However, one can expect all cookies suitable to be sent will be
     * accepted.
     */
    boolean accept(Cookie cookie, URL url);
    
    /**
     * Returns whether the cookie should be sent for the given URL.
     */
    boolean matches(Cookie cookie, URL url);
}
