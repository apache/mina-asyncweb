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
package org.safehaus.asyncweb.codec.encoder;

import java.util.Collection;

import org.apache.mina.common.IoBuffer;
import org.safehaus.asyncweb.common.Cookie;

/**
 * Defines a strategy for encoding cookies
 *
 * @author irvingd
 *
 */
public interface CookieEncoder {

    /**
     * Encodes a list of {@link Cookie}s in to the specified buffer
     *
     * @param cookies  The cookies
     * @param buffer   The buffer
     */
    public void encodeCookie(Collection<Cookie> cookies, IoBuffer buffer);
}
