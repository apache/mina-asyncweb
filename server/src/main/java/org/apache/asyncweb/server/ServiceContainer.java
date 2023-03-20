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

import java.util.List;

import org.apache.asyncweb.server.context.KeepAliveStrategy;
import org.apache.asyncweb.server.errorReporting.ErrorResponseFormatter;
import org.apache.asyncweb.server.session.HttpSessionAccessor;

public interface ServiceContainer {

    /**
     * Adds a {@link HttpServiceFilter} to this container.
     * Requests dispatched to this container are run through filters
     * in the order they are added
     *
     * @param handler  The handler to add
     * @throws IllegalStateException If this container has been started
     */
    void addServiceFilter(HttpServiceFilter handler);

    /**
     * Adds a <code>Transport</code> to this container.
     * The transport is provided with a <code></code>, and is started
     * when this container starts
     *
     * @param transport  The transport to add
     * @throws IllegalStateException If this container has been started
     */
    void addTransport(Transport transport);

    /**
     * Returns the read-only {@link List} of {@link HttpServiceFilter}s.
     */
    List<HttpServiceFilter> getServiceFilters();

    /**
     * Returns the employed {@link KeepAliveStrategy} of this container.
     */
    KeepAliveStrategy getKeepAliveStrategy();

    /**
     * Returns the employes {@link HttpSessionAccessor} to be supplied to each request
     * as it passes through the container.
     * The accessor is shutdown when this container is stopped
     */
    HttpSessionAccessor getSessionAccessor();

    /**
     * Sets the <code>SessionAccessor</code> to be supplied to each request
     * as it passes through the container.
     * The accessor is shutdown when this container is stopped
     *
     * @param accessor  The accessor
     */
    void setSessionAccessor( HttpSessionAccessor accessor);

    ErrorResponseFormatter getErrorResponseFormatter();

    boolean isSendServerHeader();

    /**
     * Starts this container.
     * Requests may be dispatched to this container after it has been
     * started.
     * During start-up, this container starts all associated transports
     * and service handlers.
     *
     * @throws ContainerLifecycleException If a transport fails to start
     */
    void start() throws ContainerLifecycleException;

    /**
     * Stops this container.
     * During shut-down, this container stops all associated transports
     * and service handlers.
     */
    void stop();
}
