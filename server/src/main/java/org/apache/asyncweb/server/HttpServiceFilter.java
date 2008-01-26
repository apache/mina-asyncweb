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
package org.apache.asyncweb.server;

public interface HttpServiceFilter {

    /**
     * Notifies this handler of the incoming request for a specified request.
     *
     * This handler should call <code>invokeNext</code> when it has completed
     * its duties. This invocation may occur asyncronously - but <i>must</i>
     * occur for each notification
     *
     * @param next           The next filter in the filter chain
     * @param context        The service context
     */
    void handleRequest(NextFilter next, HttpServiceContext context)
            throws Exception;

    /**
     * Notifies this handler of the committed response for a specified request.
     *
     * This handler should call <code>invokeNext</code> when it has completed
     * its duties. This invocation may occur asyncronously - but <i>must</i>
     * occur for each notification
     *
     * @param next           The next filter in the filter chain
     * @param context        The service context
     */
    void handleResponse(NextFilter next, HttpServiceContext context)
            throws Exception;

    void start();

    void stop();

    /**
     * Encapsuates a location within a chain of tasks to be performed.
     *
     * @author irvingd
     * @author trustin
     * @version $Rev$, $Date$
     */
    public interface NextFilter {

        /**
         * Causes the next task in the chain to be performed
         */
        void invoke();
    }
}
