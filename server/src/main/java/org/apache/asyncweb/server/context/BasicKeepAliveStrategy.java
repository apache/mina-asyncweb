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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Defines common sensible behaviour for the rules under which a
 * connection should be kept alive.
 * <code>BasicKeepAliveStrategy</code> will signal that a connection
 * should be closed iff:
 * <ul>
 *   <li>The request is <i>committed</i></li>
 *   <li>The status of a response forces the connection to be closed</li>
 *   <li>The request does not want keep-alives</li>
 * </ul>
 * Implementations should override <code></code> to provide additional
 * constraints
 *
 * @author irvingd
 *
 */
public class BasicKeepAliveStrategy implements KeepAliveStrategy {

    private static final Logger LOG = LoggerFactory
            .getLogger(BasicKeepAliveStrategy.class);

    /**
     * Determines whether a connection should be kept alive based
     * on a response.
     * This method returns true iff either:
     * <ul>
     *   <li>The request is not committed</li>
     * </u>
     * or (in order):
     * <ul>
     *   <li>The status of a response does not force closure</li>
     *   <li>The request indicates that it wants the connection to remain
     *       open</li>
     *   <li><code>doIsKeepAlive</code> returns true</li>
     * </ul>
     *
     * @param response  The response to examine
     * @return <code>true</code> iff a connection should be kept alive
     */
    public final boolean keepAlive(HttpServiceContext context,
            HttpResponse response) {
        boolean isKeepAlive;
        if (!context.isResponseCommitted()) {
            isKeepAlive = true;
        } else {
            isKeepAlive = !response.getStatus().forcesConnectionClosure()
                    && context.getRequest().isKeepAlive()
                    && doIsKeepAlive(context, response);
            if (!isKeepAlive && LOG.isDebugEnabled()
                    && response.getStatus().forcesConnectionClosure()) {
                LOG.debug("Response status forces closure: "
                        + response.getStatus());
            }
        }
        return isKeepAlive;
    }

    /**
     * Simply returns <code>true</code> by default.
     * Implementations should override this method to provide any
     * additional keep-alive conditions
     *
     * @param context   The context to check
     * @param response  The request to check
     * @return          <code>true</code> if the connection should
     *                  be kept alive
     */
    @SuppressWarnings("unused")
    protected boolean doIsKeepAlive(HttpServiceContext context,
            HttpResponse response) {
        return true;
    }

}
