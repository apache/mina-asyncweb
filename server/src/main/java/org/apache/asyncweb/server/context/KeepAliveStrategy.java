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
package org.apache.asyncweb.server.context;

import org.apache.asyncweb.common.HttpResponse;
import org.apache.asyncweb.server.HttpServiceContext;

/**
 * Defines a strategy for deciding whether a connection should
 * remain open after a response has been handled
 *
 */
public interface KeepAliveStrategy {

    /**
     * Determines whether a connection should remain open after a
     * response has been handled
     *
     * @param context   The context to check
     * @param response  The response to check
     * @return          <code>true</code> iff a connection should
     *                  remain open after processing the specified response
     */
    public boolean keepAlive( HttpServiceContext context, HttpResponse response);

}
