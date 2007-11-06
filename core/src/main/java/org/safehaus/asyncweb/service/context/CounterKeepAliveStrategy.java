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
package org.safehaus.asyncweb.service.context;

import org.safehaus.asyncweb.common.HttpResponse;
import org.safehaus.asyncweb.service.HttpServiceContext;

public class CounterKeepAliveStrategy extends BasicKeepAliveStrategy {

    private static final int DEFAULT_KEEP_ALIVES = 100;

    private int maxKeepAlives = DEFAULT_KEEP_ALIVES;

    private int keptAliveCount;

    /**
     * @param maxKeepAlives  The maximum number of requests for which
     *                       a connection should be kept alive
     */
    public CounterKeepAliveStrategy(int maxKeepAlives) {
        this.maxKeepAlives = maxKeepAlives;
    }

    /**
     * Determines whether a connection should be "kept alive" based on
     * the number of requests for which the connection has previously
     * been kept alive.
     *
     * @param response  The response to check
     */
    @Override
    protected boolean doIsKeepAlive(HttpServiceContext context,
            HttpResponse response) {
        return ++keptAliveCount < maxKeepAlives;
    }
}
